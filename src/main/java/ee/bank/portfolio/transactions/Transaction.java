package ee.bank.portfolio.transactions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

public record Transaction(
        UUID id,
        Instant timestamp,
        String type,
        int quantity,
        BigDecimal price,
        BigDecimal fee
) {
    public BigDecimal getBuyTotalCost(){
       return price().multiply(BigDecimal.valueOf(quantity())).add(fee());
    }

    public BigDecimal getBuyAverageCost(){
        return getBuyTotalCost().divide(BigDecimal.valueOf(quantity()), 6, RoundingMode.HALF_UP);
    }

    public BigDecimal getSellProceeds(){
        return price().multiply(BigDecimal.valueOf(quantity())).subtract(fee());
    }
}
