package ee.bank.portfolio.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.bank.portfolio.model.Transaction;
import ee.bank.portfolio.service.TransactionService;
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

    private final TransactionService transactionService;
    private final ObjectMapper objectMapper;

    public TransactionDataLoader(TransactionService transactionService, ObjectMapper objectMapper) {
        this.transactionService = transactionService;
        this.objectMapper = objectMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadInitialDataIfNeeded() throws IOException {
        if (transactionService.getAllTransactions().isEmpty()) {
            var resource = new ClassPathResource("transactions/data.json");
            List<Transaction> transactions = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
            transactions.forEach(transactionService::handleAddTransaction);
        }
    }
}
