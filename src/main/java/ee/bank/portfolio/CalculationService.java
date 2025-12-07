package ee.bank.portfolio;

import ee.bank.portfolio.transactions.Transaction;
import ee.bank.portfolio.transactions.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalculationService {

    private final TransactionRepository transactionRepository;

    public CalculationService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void handleAddTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    public List<Transaction> getAllTransactions(){
        return transactionRepository.getAll();
    }

    public void addTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }
}
