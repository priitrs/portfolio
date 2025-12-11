package ee.bank.portfolio.model;

import java.math.BigDecimal;

public record AssetProfitabilityDto(
        String asset,
        int remainingQuantity,
        BigDecimal remainingAverageCost,
        BigDecimal remainingCostBasis,
        BigDecimal realizedProfitLoss,
        BigDecimal gainLoss,
        BigDecimal totalInvested,
        BigDecimal totalReturn,
        String totalReturnPercentage
) {
}
