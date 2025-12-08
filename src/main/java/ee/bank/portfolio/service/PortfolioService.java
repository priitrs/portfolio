package ee.bank.portfolio.service;

import ee.bank.portfolio.model.PortfolioProfitabilityDto;
import ee.bank.portfolio.model.Position;
import ee.bank.portfolio.repository.PositionRepository;
import ee.bank.portfolio.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static ee.bank.portfolio.service.TransactionService.DEFAULT_ASSET;

@Service
@AllArgsConstructor
public class PortfolioService {

    private final TransactionRepository transactionRepository;
    private final PositionRepository positionRepository;

    public PortfolioProfitabilityDto getProfitability() {
        var position = positionRepository.getByAsset(DEFAULT_ASSET)
                .orElse(new Position(DEFAULT_ASSET, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        var totalInvested = transactionRepository.findTotalInvested();
        var totalProfit = position.realizedProfitLoss();
        var totalReturn = totalProfit.divide(totalInvested, 6, RoundingMode.HALF_UP);
        String totalReturnPercentage = totalReturn.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
        return new PortfolioProfitabilityDto(
                position.quantity(),
                position.averageCost(),
                position.totalCost(),
                position.realizedProfitLoss(),
                totalProfit,
                totalInvested,
                totalReturn,
                totalReturnPercentage
        );
    }
}
