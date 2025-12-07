package ee.bank.portfolio;

import ee.bank.portfolio.transactions.Transaction;
import ee.bank.portfolio.transactions.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainerProvider;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest()
@Testcontainers
@AutoConfigureMockMvc
class TransactionsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired private TransactionRepository transactionRepository;

    @Container
    @ServiceConnection
    static JdbcDatabaseContainer<?> postgres = new PostgreSQLContainerProvider().newInstance("15");

    @Test @Transactional
    void getAllTransactions() throws Exception {
        var buyTransaction = transactionRepository.save(new Transaction(null, OffsetDateTime.parse("2024-01-01T10:00:00Z"), "buy", 10, BigDecimal.valueOf(5), BigDecimal.valueOf(2)));
        var sellTransaction = transactionRepository.save(new Transaction(null, OffsetDateTime.parse("2024-01-01T11:00:00Z"), "sell", 5, BigDecimal.valueOf(5), BigDecimal.valueOf(2)));

        mockMvc.perform(get("/api/portfolio/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value(buyTransaction.type()))
                .andExpect(jsonPath("$[1].type").value(sellTransaction.type()));;
    }

    @Test @Transactional
    void addTransaction() throws Exception {
        String payload = """
    {
        "id": "5aa66693-1c53-4ce7-bac6-d8eabd2cb6b1",
        "timestamp": "2024-01-01T10:00:00Z",
        "type": "buy",
        "quantity": 10,
        "price": 5,
        "fee": 2
    }
    """;

        mockMvc.perform(post("/api/portfolio/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        var result = transactionRepository.getAll();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().type()).isEqualTo("buy");
    }
}
