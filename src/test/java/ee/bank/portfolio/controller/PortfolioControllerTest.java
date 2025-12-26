package ee.bank.portfolio.controller;

import ee.bank.portfolio.model.AssetProfitabilityDto;
import ee.bank.portfolio.model.TransactionDto;
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

    @Test @Transactional
    void getProfitability() {
        transactionService.handleAddTransaction(new TransactionDto("ASSET", Instant.parse("2024-01-01T10:00:00Z"), "buy", 10, BigDecimal.valueOf(5), BigDecimal.valueOf(2)));
        transactionService.handleAddTransaction(new TransactionDto( "ASSET", Instant.parse("2024-01-01T11:00:00Z"), "sell", 5, BigDecimal.valueOf(6), BigDecimal.valueOf(3)));

        var result = controller.getProfitability();

        assertThat(result.size()).isEqualTo(1);
        AssetProfitabilityDto profitabilityDto = result.getFirst();
        assertThat(profitabilityDto.remainingQuantity()).isEqualTo(5);
        assertThat(profitabilityDto.remainingAverageCost()).isEqualByComparingTo(BigDecimal.valueOf(5.2));
        assertThat(profitabilityDto.remainingCostBasis()).isEqualByComparingTo(BigDecimal.valueOf(26));
        assertThat(profitabilityDto.realizedProfitLoss()).isEqualByComparingTo(BigDecimal.valueOf(1));
        assertThat(profitabilityDto.gainLoss()).isEqualByComparingTo(BigDecimal.valueOf(1));
        assertThat(profitabilityDto.totalInvested()).isEqualByComparingTo(BigDecimal.valueOf(52));
        assertThat(profitabilityDto.totalReturn()).isEqualByComparingTo(BigDecimal.valueOf(0.0192308));
    }

    @Test @Transactional
    void getPositions() {
        transactionService.handleAddTransaction(new TransactionDto( "ASSET_1", Instant.parse("2024-01-01T10:00:00Z"), "buy", 10, BigDecimal.valueOf(5), BigDecimal.valueOf(2)));
        transactionService.handleAddTransaction(new TransactionDto("ASSET_2", Instant.parse("2024-01-01T10:00:00Z"), "buy", 10, BigDecimal.valueOf(5), BigDecimal.valueOf(2)));

        var result = controller.getPositions();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test @Transactional
    void getPositionLots() {
        transactionService.handleAddTransaction(new TransactionDto("ASSET_1", Instant.parse("2024-01-01T10:00:00Z"), "buy", 10, BigDecimal.valueOf(5), BigDecimal.valueOf(2)));
        transactionService.handleAddTransaction(new TransactionDto("ASSET_2", Instant.parse("2024-01-01T10:00:00Z"), "buy", 10, BigDecimal.valueOf(5), BigDecimal.valueOf(2)));

        var result = controller.getPositionLots("ASSET_2");

        assertThat(result.size()).isEqualTo(1);
    }
}
