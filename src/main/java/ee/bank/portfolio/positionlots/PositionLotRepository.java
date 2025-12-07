package ee.bank.portfolio.positionlots;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class PositionLotRepository {

    private final JdbcTemplate jdbcTemplate;

    public PositionLotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(String asset, int quantity, BigDecimal unitCost) {
        jdbcTemplate.update("INSERT INTO position_lots (asset, qty_remaining, unit_cost) VALUES (?, ?, ?);",
                asset, quantity, unitCost);
    }
}
