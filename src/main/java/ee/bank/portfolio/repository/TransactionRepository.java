package ee.bank.portfolio.repository;

import ee.bank.portfolio.model.Transaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public class TransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Transaction> transactionRowMapper = (rs, rowNum) -> new Transaction(
            rs.getObject("id", UUID.class),
            rs.getTimestamp("timestamp").toInstant(),
            rs.getString("type"),
            rs.getInt("quantity"),
            rs.getBigDecimal("price"),
            rs.getBigDecimal("fee")
    );

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Transaction> getAll(){
        return jdbcTemplate.query("SELECT * FROM transactions;", transactionRowMapper);
    }

    public Transaction save(Transaction t) {
        return jdbcTemplate.queryForObject("INSERT INTO transactions (timestamp, type, quantity, price, fee) VALUES (?, ?, ?, ?, ?) RETURNING *;",
                transactionRowMapper, java.sql.Timestamp.from(t.timestamp()), t.type(), t.quantity(), t.price(), t.fee());
    }

    public BigDecimal findTotalInvested() {
        BigDecimal total = jdbcTemplate.queryForObject(
                "SELECT SUM(quantity * price + fee) AS total_sum FROM transactions WHERE type = 'buy';",
                BigDecimal.class
        );
        return total != null ? total : BigDecimal.ZERO;    }
}
