package ee.bank.portfolio.positionlots;

import java.math.BigDecimal;

public record PositionLot(
        long id,
        String asset,
        int qtyRemaining,
        BigDecimal unitCost
) {
}
