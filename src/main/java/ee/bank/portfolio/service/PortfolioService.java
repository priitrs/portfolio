package ee.bank.portfolio.service;

import ee.bank.portfolio.model.*;
import ee.bank.portfolio.repository.PositionLotRepository;
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

    public List<PositionLotDto> getPositionsLots(String asset) {
        return positionLotRepository.findByAssetOrderByIdAsc(asset).stream()
                .map(PositionLot::toDto)
                .toList();
    }

    private AssetProfitabilityDto getAssetProfitability(String asset) {
        var position = getPosition(asset);
        var totalInvested = transactionRepository.findTotalInvested(asset);
        var totalProfit = position.getRealizedProfitLoss();
        var totalReturn = totalProfit.divide(totalInvested, 6, RoundingMode.HALF_UP);
        String totalReturnPercentage = totalReturn.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
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
