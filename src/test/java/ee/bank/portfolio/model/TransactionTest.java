package ee.bank.portfolio.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionTest {

    Transaction transaction = new Transaction(null, "ASSET", Instant.parse("2024-01-01T10:00:00Z"), "buy",2, BigDecimal.TEN, BigDecimal.ONE);

    @Test
    void getBuyTotalCost() {
        assertThat(transaction.getBuyTotalCost()).isEqualByComparingTo(BigDecimal.valueOf(21));
    }

    @Test
    void getBuyAverageCost() {
        assertThat(transaction.getBuyAverageCost()).isEqualByComparingTo(BigDecimal.valueOf(10.5));
    }

    @Test
    void getSellProceeds() {
        assertThat(transaction.getSellProceeds()).isEqualByComparingTo(BigDecimal.valueOf(19));
    }

    @Test
    void toDto() {
        assertThat(transaction.toDto()).isEqualTo(new TransactionDto("ASSET", Instant.parse("2024-01-01T10:00:00Z"), "buy", 2, BigDecimal.TEN, BigDecimal.ONE));
    }
}