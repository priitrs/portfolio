package ee.bank.portfolio.model;

import java.math.BigDecimal;

public record PositionLot(
        long id,
        String asset,
        int qtyRemaining,
        BigDecimal unitCost
) {
}
