package ee.bank.portfolio.controller;

import ee.bank.portfolio.model.Transaction;
import ee.bank.portfolio.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainerProvider;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest()
@Testcontainers
class PortfolioControllerTest {

    @Autowired private PortfolioController controller;
    @Autowired private TransactionService transactionService;

    @Container
    @ServiceConnection
    static JdbcDatabaseContainer<?> postgres = new PostgreSQLContainerProvider().newInstance("15");

    @Test
    @Transactional
    void getProfitability() {
        transactionService.handleAddTransaction(new Transaction(null, Instant.parse("2024-01-01T10:00:00Z"), "buy", 10, BigDecimal.valueOf(5), BigDecimal.valueOf(2)));
        transactionService.handleAddTransaction(new Transaction(null, Instant.parse("2024-01-01T11:00:00Z"), "sell", 5, BigDecimal.valueOf(6), BigDecimal.valueOf(3)));

        var result = controller.getProfitability();

        assertThat(result.remainingQuantity()).isEqualTo(5);
        assertThat(result.remainingAverageCost()).isEqualByComparingTo(BigDecimal.valueOf(5.2));
        assertThat(result.remainingCostBasis()).isEqualByComparingTo(BigDecimal.valueOf(26));
        assertThat(result.realizedProfitLoss()).isEqualByComparingTo(BigDecimal.valueOf(1));
        assertThat(result.gainLoss()).isEqualByComparingTo(BigDecimal.valueOf(1));
        assertThat(result.totalInvested()).isEqualByComparingTo(BigDecimal.valueOf(52));
        assertThat(result.totalReturn()).isEqualByComparingTo(BigDecimal.valueOf(0.019231));
    }
}
