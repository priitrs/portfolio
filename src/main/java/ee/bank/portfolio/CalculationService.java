package ee.bank.portfolio;

import ee.bank.portfolio.transactions.Transaction;
import ee.bank.portfolio.transactions.TransactionRepository;

import java.util.List;

@org.springframework.stereotype.Service
public class CalculationService {

    private final TransactionRepository transactionRepository;

    public CalculationService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> getAllTransactions(){
        return transactionRepository.getAll();
    }

    public void addTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }
}
