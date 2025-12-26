package ee.bank.portfolio.controller;

import ee.bank.portfolio.model.AssetProfitabilityDto;
import ee.bank.portfolio.model.PositionDto;
import ee.bank.portfolio.model.PositionLotDto;
import ee.bank.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Portfolio", description = "Information about portfolio")
@RestController
@RequestMapping("/api/portfolio")
@AllArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @Operation(summary = "Get portfolio profitability")
    @GetMapping("/profitability")
    public List<AssetProfitabilityDto> getProfitability(){
        return portfolioService.getProfitability();
    }

    @Operation(summary = "Get portfolio positions")
    @GetMapping("/positions")
    public List<PositionDto> getPositions(){
        return portfolioService.getPositions();
    }

    @Operation(summary = "Get positions lots")
    @GetMapping("/positions/{asset}/lots")
    public List<PositionLotDto> getPositionLots(@PathVariable String asset){
        return portfolioService.getPositionLots(asset);
    }
}
