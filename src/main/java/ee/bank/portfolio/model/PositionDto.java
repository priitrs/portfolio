package ee.bank.portfolio.model;

import java.math.BigDecimal;

public record PositionDto(
        String asset,
        int quantity,
        BigDecimal averageCost,
        BigDecimal totalCost,
        BigDecimal realizedProfitLoss
) {
}
