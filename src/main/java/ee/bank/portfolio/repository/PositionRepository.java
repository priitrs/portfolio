package ee.bank.portfolio.repository;

import ee.bank.portfolio.model.Position;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@AllArgsConstructor
public class PositionRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Position> positionRowMapper = (rs, rowNum) -> new Position(
            rs.getString("asset"),
            rs.getInt("quantity"),
            rs.getBigDecimal("average_cost"),
            rs.getBigDecimal("total_cost"),
            rs.getBigDecimal("realized_pl")
    );

    public Optional<Position> getByAsset(String asset) {
        return jdbcTemplate.query("SELECT * FROM positions WHERE asset = ?;", positionRowMapper, asset).stream().findFirst();
    }

    public void insert(Position p) {
        jdbcTemplate.update("INSERT INTO positions (asset, quantity, average_cost, total_cost, realized_pl) VALUES (?, ?, ?, ?, ?);",
                p.asset(), p.quantity(), p.averageCost(), p.totalCost(), p.realizedProfitLoss());
    }

    public void update(Position p) {
        jdbcTemplate.update("UPDATE positions SET quantity = ?, average_cost = ?, total_cost = ?, realized_pl = ? WHERE asset = ?;",
                p.quantity(), p.averageCost(), p.totalCost(), p.realizedProfitLoss(), p.asset());
    }
}
