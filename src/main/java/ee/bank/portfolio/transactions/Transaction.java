package ee.bank.portfolio.transactions;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    public BigDecimal getTotalCost(){
       return price().multiply(BigDecimal.valueOf(quantity())).add(fee());
    }

    public BigDecimal getAverageCost(){
        return getTotalCost().divide(BigDecimal.valueOf(quantity()), 6, RoundingMode.HALF_UP);

    }
}
