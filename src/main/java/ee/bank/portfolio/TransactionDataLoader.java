package ee.bank.portfolio;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.bank.portfolio.transactions.Transaction;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Profile("!test")
public class TransactionDataLoader {

    private final CalculationService calculationService;
    private final ObjectMapper objectMapper;

    public TransactionDataLoader(CalculationService calculationService, ObjectMapper objectMapper) {
        this.calculationService = calculationService;
        this.objectMapper = objectMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadInitialDataIfNeeded() throws IOException {
        if (calculationService.getAllTransactions().isEmpty()) {
            var resource = new ClassPathResource("transactions/data.json");
            List<Transaction> transactions = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
            transactions.forEach(calculationService::handleAddTransaction);
        }
    }
}
