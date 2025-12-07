package ee.bank.portfolio.transactions;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record Transaction(
        UUID id,
        OffsetDateTime timestamp,
        String type,
        int quantity,
        BigDecimal price,
        BigDecimal fee
) {
}
