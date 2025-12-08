package ee.bank.portfolio.controller;

import ee.bank.portfolio.model.PortfolioProfitabilityDto;
import ee.bank.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio/profitability")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @Operation(summary = "Get portfolio profitability")
    @GetMapping()
    public PortfolioProfitabilityDto getProfitability(){
        return portfolioService.getProfitability();
    }
}
