package ee.bank.portfolio;

import ee.bank.portfolio.transactions.Transaction;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
public class Controller {

    private final CalculationService calculationService;

    public Controller(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @GetMapping("/transactions")
    public List<Transaction> getAllTransactions(){
        return calculationService.getAllTransactions();
    }

    @PostMapping("/transactions")
    public void addTransaction(@RequestBody Transaction transaction){
        calculationService.addTransaction(transaction);
    }
}
