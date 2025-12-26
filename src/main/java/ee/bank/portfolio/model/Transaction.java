package ee.bank.portfolio.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static ee.bank.portfolio.config.MathContexts.FINANCE;

@Entity
@Table(name = "transactions")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Transaction{
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;
    private String asset;
    private Instant timestamp;
    private String type;
    private int quantity;
    private BigDecimal price;
    private BigDecimal fee;

    public BigDecimal getBuyTotalCost(){
       return getPrice()
               .multiply(BigDecimal.valueOf(getQuantity()))
               .add(getFee(), FINANCE);
    }

    public BigDecimal getBuyAverageCost(){
        return getBuyTotalCost()
                .divide(BigDecimal.valueOf(getQuantity()), FINANCE);
    }

    public BigDecimal getSellProceeds(){
        return getPrice()
                .multiply(BigDecimal.valueOf(getQuantity()))
                .subtract(getFee(), FINANCE);
    }

    public TransactionDto toDto(){
        return new TransactionDto(getAsset(), getTimestamp(), getType(), getQuantity(), getPrice(), getFee());
    }
}
