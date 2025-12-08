package ee.bank.portfolio.controller;

import ee.bank.portfolio.model.PortfolioProfitabilityDto;
import ee.bank.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio/profitability")
@AllArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @Operation(summary = "Get portfolio profitability")
    @GetMapping()
    public PortfolioProfitabilityDto getProfitability(){
        return portfolioService.getProfitability();
    }
}
