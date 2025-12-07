package ee.bank.portfolio.transactions;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionTest {

    Transaction transaction = new Transaction(null, null, null, 2, BigDecimal.TEN, BigDecimal.ONE);

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
}