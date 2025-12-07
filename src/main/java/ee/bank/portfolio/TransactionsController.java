package ee.bank.portfolio;

import ee.bank.portfolio.transactions.Transaction;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio/transactions")
public class TransactionsController {

    private final CalculationService calculationService;

    public TransactionsController(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @GetMapping()
    public List<Transaction> getAllTransactions(){
        return calculationService.getAllTransactions();
    }

    @PostMapping()
    public void addTransaction(@RequestBody Transaction transaction){
        calculationService.handleAddTransaction(transaction);
    }
}
