package ee.bank.portfolio.model;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionDto(
        String asset,
        Instant timestamp,
        String type,
        int quantity,
        BigDecimal price,
        BigDecimal fee
) {
}
