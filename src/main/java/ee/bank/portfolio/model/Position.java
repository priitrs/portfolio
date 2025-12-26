package ee.bank.portfolio.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

import java.math.BigDecimal;

@Entity
@Table(name = "positions")
@With
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Position {
    @Id
    private String asset;
    private int quantity;
    private BigDecimal averageCost;
    private BigDecimal totalCost;
    private BigDecimal realizedProfitLoss;

    public PositionDto toDto() {
        return new PositionDto(asset, quantity, averageCost, totalCost, realizedProfitLoss);
    }
}
