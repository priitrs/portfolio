package ee.bank.portfolio.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

public record Transaction(
        @JsonIgnore UUID id,
        Instant timestamp,
        String type,
        int quantity,
        BigDecimal price,
        BigDecimal fee
) {
    @JsonIgnore
    public BigDecimal getBuyTotalCost(){
       return price().multiply(BigDecimal.valueOf(quantity())).add(fee());
    }

    @JsonIgnore
    public BigDecimal getBuyAverageCost(){
        return getBuyTotalCost().divide(BigDecimal.valueOf(quantity()), 6, RoundingMode.HALF_UP);
    }

    @JsonIgnore
    public BigDecimal getSellProceeds(){
        return price().multiply(BigDecimal.valueOf(quantity())).subtract(fee());
    }
}
