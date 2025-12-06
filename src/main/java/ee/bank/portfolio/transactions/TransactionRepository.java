package ee.bank.portfolio.transactions;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class TransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Transaction> transactionRowMapper = (rs, rowNum) -> new Transaction(
            rs.getObject("id", UUID.class),
            rs.getObject("timestamp", java.time.OffsetDateTime.class),
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

    public void save(Transaction t) {
        jdbcTemplate.update("INSERT INTO transactions (timestamp, type, quantity, price, fee) VALUES (?, ?, ?, ?, ?);",
                t.timestamp(), t.type(), t.quantity(), t.price(), t.fee());
    }
}
