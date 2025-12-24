package ee.bank.portfolio.controller;

import ee.bank.portfolio.exception.TransactionException;
import ee.bank.portfolio.model.TransactionDto;
import ee.bank.portfolio.repository.PositionLotRepository;
import ee.bank.portfolio.repository.PositionRepository;
import ee.bank.portfolio.repository.TransactionRepository;
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
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest()
@Testcontainers
@AutoConfigureMockMvc
class TransactionsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired private TransactionsController controller;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private PositionLotRepository positionLotRepository;
    @Autowired private PositionRepository positionRepository;

    @Container
    @ServiceConnection
    static JdbcDatabaseContainer<?> postgres = new PostgreSQLContainerProvider().newInstance("15");

    @Test @Transactional
    void getAllTransactions() throws Exception {
        var buyTransaction = transactionRepository.save(new TransactionDto( "ASSET", Instant.parse("2024-01-01T10:00:00Z"), "buy", 10, BigDecimal.valueOf(5), BigDecimal.valueOf(2)));
        var sellTransaction = transactionRepository.save(new TransactionDto( "ASSET", Instant.parse("2024-01-01T11:00:00Z"), "sell", 5, BigDecimal.valueOf(5), BigDecimal.valueOf(2)));

        mockMvc.perform(get("/api/portfolio/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value(buyTransaction.type()))
                .andExpect(jsonPath("$[1].type").value(sellTransaction.type()));
    }

    @Test @Transactional
    void addTransaction() throws Exception {
        String payload = """
    {
        "id": "5aa66693-1c53-4ce7-bac6-d8eabd2cb6b1",
        "asset": "ASSET",
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

    @Test @Transactional
    void addTransaction_differentOrderTypes() {
        controller.addTransaction(new TransactionDto( "ASSET", Instant.parse("2024-01-01T10:00:00Z"), "buy", 1, BigDecimal.valueOf(10), BigDecimal.valueOf(1)));
        controller.addTransaction(new TransactionDto( "ASSET", Instant.parse("2024-01-01T10:30:00Z"), "buy", 3, BigDecimal.valueOf(11), BigDecimal.valueOf(3)));
        controller.addTransaction(new TransactionDto( "ASSET", Instant.parse("2024-01-01T11:00:00Z"), "sell", 3, BigDecimal.valueOf(20), BigDecimal.valueOf(2)));

        var positionLots = positionLotRepository.getAllByAsset("ASSET");
        assertThat(positionLots.size()).isEqualTo(2);
        var positionLot = positionLots.getFirst();
        assertThat(positionLot.qtyRemaining()).isEqualTo(0);
        assertThat(positionLot.unitCost()).isEqualByComparingTo(BigDecimal.valueOf(11));
        var positionLot2 = positionLots.get(1);
        assertThat(positionLot2.qtyRemaining()).isEqualTo(1);
        assertThat(positionLot2.unitCost()).isEqualByComparingTo(BigDecimal.valueOf(12));
        var position = positionRepository.getByAsset("ASSET");
        assertThat(position.isPresent()).isTrue();
        assertThat(position.get().quantity()).isEqualTo(1);
        assertThat(position.get().averageCost()).isEqualByComparingTo(BigDecimal.valueOf(12));
        assertThat(position.get().totalCost()).isEqualByComparingTo(BigDecimal.valueOf(12));
        assertThat(position.get().realizedProfitLoss()).isEqualByComparingTo(BigDecimal.valueOf(23));
    }

    @Test @Transactional
    void addTransaction_firstIsSellOrder_noPosition() {
        var transaction = new TransactionDto( "ASSET", Instant.parse("2024-01-01T10:00:00Z"), "sell", 2, BigDecimal.valueOf(5), BigDecimal.valueOf(2));
        assertThatThrownBy(() -> controller.addTransaction(transaction))
                .isInstanceOf(TransactionException.class)
                .hasMessage("Position does not exist for sell order. Asset: ASSET");
    }

    @Test @Transactional
    void addTransaction_secondIsSellOrder_tooSmallPosition() {
        controller.addTransaction(new TransactionDto( "ASSET", Instant.parse("2024-01-01T10:00:00Z"), "buy", 2, BigDecimal.valueOf(5), BigDecimal.valueOf(2)));
        var sellTransaction = new TransactionDto( "ASSET", Instant.parse("2024-01-01T11:00:00Z"), "sell", 4, BigDecimal.valueOf(11), BigDecimal.valueOf(4));

        assertThatThrownBy(() -> controller.addTransaction(sellTransaction))
                .isInstanceOf(TransactionException.class)
                .hasMessage("Existing position is too small for sell order. Position qty: 2, transaction qty: 4");
    }
}
