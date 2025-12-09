package ee.bank.portfolio.repository;

import ee.bank.portfolio.model.PositionLot;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@AllArgsConstructor
public class PositionLotRepository {

    private final JdbcTemplate jdbcTemplate;

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

    public List<PositionLot> getAllByAsset(String asset){
        return jdbcTemplate.query("SELECT * FROM position_lots WHERE asset = ? ORDER BY id;", positionLotRowMapper, asset);
    }

    public PositionLot getFirstWithRemainingQuantity(){
        return jdbcTemplate.query("SELECT * FROM position_lots WHERE qty_remaining > 0 LIMIT 1;", positionLotRowMapper).getFirst();
    }

    public void updateQuantity(long id, int quantity) {
        jdbcTemplate.update("UPDATE position_lots SET qty_remaining = ? WHERE id = ?;", quantity, id);
    }
}
