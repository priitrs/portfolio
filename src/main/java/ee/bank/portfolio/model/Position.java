package ee.bank.portfolio.model;

import lombok.With;

import java.math.BigDecimal;

@With
public record Position(
        String asset,
        int quantity,
        BigDecimal averageCost,
        BigDecimal totalCost,
        BigDecimal realizedProfitLoss
) {
}
