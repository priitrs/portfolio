package ee.bank.portfolio.positions;

import java.math.BigDecimal;

public record Position(
        String asset,
        int quantity,
        BigDecimal averageCost,
        BigDecimal totalCost,
        BigDecimal realizedProfitLoss
) {
}
