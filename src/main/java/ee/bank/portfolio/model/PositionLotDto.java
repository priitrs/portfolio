package ee.bank.portfolio.model;

import java.math.BigDecimal;

public record PositionLotDto(
        String asset,
        int qtyRemaining,
        BigDecimal unitCost
) {
}
