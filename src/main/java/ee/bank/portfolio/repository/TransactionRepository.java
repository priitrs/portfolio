package ee.bank.portfolio.repository;

import ee.bank.portfolio.model.Transaction;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static java.sql.Timestamp.*;

@Repository
@AllArgsConstructor
public class TransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Transaction> transactionRowMapper = (rs, rowNum) -> new Transaction(
            rs.getObject("id", UUID.class),
            rs.getString("asset"),
            rs.getTimestamp("timestamp").toInstant(),
            rs.getString("type"),
            rs.getInt("quantity"),
            rs.getBigDecimal("price"),
            rs.getBigDecimal("fee")
    );

    public List<Transaction> getAll(){
        return jdbcTemplate.query("SELECT * FROM transactions ORDER BY timestamp;", transactionRowMapper);
    }

    public Transaction save(Transaction t) {
        return jdbcTemplate.queryForObject("INSERT INTO transactions (asset, timestamp, type, quantity, price, fee) VALUES (?, ?, ?, ?, ?, ?) RETURNING *;",
                transactionRowMapper, t.asset(), from(t.timestamp()), t.type(), t.quantity(), t.price(), t.fee());
    }

    public BigDecimal findTotalInvested(String asset) {
        String query = "SELECT SUM(quantity * price + fee) AS total_sum FROM transactions WHERE type = 'buy' AND asset = ?;";
        return jdbcTemplate.queryForObject(query, BigDecimal.class, asset);
    }
}
