package ee.bank.portfolio.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

import java.math.BigDecimal;

@Entity
@Table(name = "position_lots")
@With
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PositionLot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String asset;
    private int qtyRemaining;
    private BigDecimal unitCost;

    public PositionLotDto toDto() {
        return new PositionLotDto(asset, qtyRemaining, unitCost);
    }
}
