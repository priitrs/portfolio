package ee.bank.portfolio.positionlots;

import ee.bank.portfolio.positions.Position;
import ee.bank.portfolio.transactions.Transaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public class PositionLotRepository {

    private final JdbcTemplate jdbcTemplate;

    public PositionLotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<PositionLot> positionLotRowMapper = (rs, rowNum) -> new PositionLot(
            rs.getLong("id"),
            rs.getString("asset"),
            rs.getInt("qty_remaining"),
            rs.getBigDecimal("unit_cost")
    );

    public void insert(String asset, int quantity, BigDecimal unitCost) {
        jdbcTemplate.update("INSERT INTO position_lots (asset, qty_remaining, unit_cost) VALUES (?, ?, ?);",
                asset, quantity, unitCost);
    }

    public List<PositionLot> getAll(){
        return jdbcTemplate.query("SELECT * FROM position_lots;", positionLotRowMapper);
    }
}
