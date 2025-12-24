package ee.bank.portfolio.controller;

import ee.bank.portfolio.model.TransactionDto;
import ee.bank.portfolio.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Transactions", description = "Information about transactions")
@RestController
@RequestMapping("/api/portfolio/transactions")
@AllArgsConstructor
public class TransactionsController {

    private final TransactionService transactionService;

    @Operation(summary = "Get all transactions")
    @GetMapping()
    public List<TransactionDto> getAllTransactions(){
        return transactionService.getAllTransactions();
    }

    @Operation(summary = "Add transaction", description = "Add buy or sell transaction")
    @PostMapping()
    public void addTransaction(@RequestBody TransactionDto transaction){
        transactionService.handleAddTransaction(transaction);
    }
}
