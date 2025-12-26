package ee.bank.portfolio.service;

import ee.bank.portfolio.model.*;
import ee.bank.portfolio.repository.PositionLotRepository;
import ee.bank.portfolio.repository.PositionRepository;
import ee.bank.portfolio.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static ee.bank.portfolio.config.MathContexts.FINANCE;
import static ee.bank.portfolio.config.MathContexts.PERCENT;

@Service
@AllArgsConstructor
public class PortfolioService {

    private final TransactionRepository transactionRepository;
    private final PositionRepository positionRepository;
    private final PositionLotRepository positionLotRepository;

    public List<AssetProfitabilityDto> getProfitability() {
        return positionRepository.findAll().stream()
                .map(Position::getAsset)
                .map(this::getAssetProfitability)
                .toList();
    }

    public List<PositionDto> getPositions() {
        return positionRepository.findAll().stream()
                .map(Position::toDto)
                .toList();
    }

    public List<PositionLotDto> getPositionLots(String asset) {
        return positionLotRepository.findByAssetOrderByIdAsc(asset).stream()
                .map(PositionLot::toDto)
                .toList();
    }

    private AssetProfitabilityDto getAssetProfitability(String asset) {
        var position = getPosition(asset);
        var totalInvested = transactionRepository.findTotalInvested(asset);
        var totalProfit = position.getRealizedProfitLoss();
        var totalReturn = totalProfit.divide(totalInvested, FINANCE);
        var totalReturnPercentage = totalReturn.multiply(BigDecimal.valueOf(100), PERCENT).toPlainString() + "%";

        return new AssetProfitabilityDto(
                asset,
                position.getQuantity(),
                position.getAverageCost(),
                position.getTotalCost(),
                position.getRealizedProfitLoss(),
                totalProfit,
                totalInvested,
                totalReturn,
                totalReturnPercentage
        );
    }

    private Position getPosition(String asset) {
        return positionRepository.findFirstByAsset(asset)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + asset));
    }
}
