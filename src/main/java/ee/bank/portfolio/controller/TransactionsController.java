package ee.bank.portfolio.controller;

import ee.bank.portfolio.service.CalculationService;
import ee.bank.portfolio.model.Transaction;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio/transactions")
public class TransactionsController {

    private final CalculationService calculationService;

    public TransactionsController(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @Operation(summary = "Get all transactions")
    @GetMapping()
    public List<Transaction> getAllTransactions(){
        return calculationService.getAllTransactions();
    }

    @Operation(summary = "Add transaction", description = "Add buy or sell transaction")
    @PostMapping()
    public void addTransaction(@RequestBody Transaction transaction){
        calculationService.handleAddTransaction(transaction);
    }
}
