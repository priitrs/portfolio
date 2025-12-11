package ee.bank.portfolio.service;

import ee.bank.portfolio.model.AssetProfitabilityDto;
import ee.bank.portfolio.model.Position;
import ee.bank.portfolio.repository.PositionRepository;
import ee.bank.portfolio.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@AllArgsConstructor
public class PortfolioService {

    private final TransactionRepository transactionRepository;
    private final PositionRepository positionRepository;

    public List<AssetProfitabilityDto> getProfitability() {
        var assetPositions = positionRepository.getAll().stream()
                .map(Position::asset)
                .toList();

        return assetPositions.stream()
                .map(this::getAssetProfitability)
                .toList();
    }

    private AssetProfitabilityDto getAssetProfitability(String asset) {
        var position = getPosition(asset);
        var totalInvested = transactionRepository.findTotalInvested(asset);
        var totalProfit = position.realizedProfitLoss();
        var totalReturn = totalProfit.divide(totalInvested, 6, RoundingMode.HALF_UP);
        String totalReturnPercentage = totalReturn.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
        return new AssetProfitabilityDto(
                asset,
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

    private Position getPosition(String asset) {
        return positionRepository.getByAsset(asset)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + asset));
    }
}
