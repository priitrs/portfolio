package ee.bank.portfolio.repository;

import ee.bank.portfolio.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("""
                SELECT COALESCE(SUM(t.quantity * t.price + t.fee), 0)
                FROM Transaction t
                WHERE t.type = 'buy'
                  AND t.asset = :asset
            """)
    BigDecimal findTotalInvested(String asset);
}
