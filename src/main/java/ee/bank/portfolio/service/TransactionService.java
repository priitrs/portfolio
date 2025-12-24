package ee.bank.portfolio.service;

import ee.bank.portfolio.exception.TransactionException;
import ee.bank.portfolio.model.Position;
import ee.bank.portfolio.model.Transaction;
import ee.bank.portfolio.model.TransactionDto;
import ee.bank.portfolio.repository.PositionLotRepository;
import ee.bank.portfolio.repository.PositionRepository;
import ee.bank.portfolio.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TransactionService {

    private static final String BUY = "buy";
    private static final String SELL = "sell";
    private final TransactionRepository transactionRepository;
    private final PositionRepository positionRepository;
    private final PositionLotRepository positionLotRepository;

    public List<TransactionDto> getAllTransactions() {
        return transactionRepository.getAll().stream()
                .map(Transaction::toDto)
                .toList();
    }

    @Transactional
    public void handleAddTransaction(TransactionDto transactionDto) {
        var optionalPosition = positionRepository.getByAsset(transactionDto.asset());
        if (BUY.equals(transactionDto.type())) {
            handleBuy(transactionDto, optionalPosition);
        } else if (SELL.equals(transactionDto.type())) {
            handleSell(transactionDto, optionalPosition);
        }
    }

    private void handleBuy(TransactionDto transactionDto, Optional<Position> optionalPosition) {
        var transaction = transactionRepository.save(transactionDto);
        positionLotRepository.insert(transaction.asset(), transaction.quantity(), transaction.getBuyAverageCost());
        if (optionalPosition.isEmpty()) {
            var newPosition = new Position(
                    transaction.asset(),
                    transaction.quantity(),
                    transaction.getBuyAverageCost(),
                    transaction.getBuyTotalCost(),
                    BigDecimal.ZERO
            );
            positionRepository.insert(newPosition);
        } else {
            positionRepository.update(getUpdatedPositionForBuy(transaction, optionalPosition.get()));
        }
    }

    private void handleSell(TransactionDto transactionDto, Optional<Position> optionalPosition) {
        var position = requirePosition(optionalPosition, transactionDto.asset());
        requireSufficientQuantity(position, transactionDto);
        var transaction = transactionRepository.save(transactionDto);
        var fifoCostBasis = processPositionLotsForFifoCostBasis(transaction);
        positionRepository.update(getUpdatedPositionForSell(transaction, position, fifoCostBasis));
    }

    private Position getUpdatedPositionForBuy(Transaction transaction, Position position) {
        int newQuantity = position.quantity() + transaction.quantity();
        var newTotalCost = position.totalCost().add(transaction.getBuyTotalCost());
        var newAverageCost = newTotalCost.divide(BigDecimal.valueOf(newQuantity), 6, RoundingMode.HALF_UP);

        return position.withQuantity(newQuantity).withAverageCost(newAverageCost).withTotalCost(newTotalCost);
    }

    private Position getUpdatedPositionForSell(Transaction transaction, Position position, BigDecimal fifoCostBasis) {
        int remainingPositionQuantity = position.quantity() - transaction.quantity();
        var remainingTotalCost = position.totalCost().subtract(fifoCostBasis);
        var updatedAverageCost = remainingPositionQuantity > 0 ?
                remainingTotalCost.divide(BigDecimal.valueOf(remainingPositionQuantity), 6, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        var updatedRealizedProfitLoss = position.realizedProfitLoss().add(transaction.getSellProceeds()).subtract(fifoCostBasis);

        return position
                .withQuantity(remainingPositionQuantity)
                .withAverageCost(updatedAverageCost)
                .withTotalCost(remainingTotalCost)
                .withRealizedProfitLoss(updatedRealizedProfitLoss);
    }

    private BigDecimal processPositionLotsForFifoCostBasis(Transaction transaction) {
        int remainingTransactionQuantity = transaction.quantity();
        var fifoCostBasis = BigDecimal.ZERO;
        while (remainingTransactionQuantity > 0) {
            int newPositionLotQuantity;
            var positionLot = positionLotRepository.getFirstWithRemainingQuantity();
            if (remainingTransactionQuantity > positionLot.qtyRemaining()) {
                fifoCostBasis = fifoCostBasis.add(positionLot.unitCost().multiply(BigDecimal.valueOf(positionLot.qtyRemaining())));
                remainingTransactionQuantity -= positionLot.qtyRemaining();
                newPositionLotQuantity = 0;
            } else {
                fifoCostBasis = fifoCostBasis.add(positionLot.unitCost().multiply(BigDecimal.valueOf(remainingTransactionQuantity)));
                newPositionLotQuantity = positionLot.qtyRemaining() - remainingTransactionQuantity;
                remainingTransactionQuantity = 0;
            }
            positionLotRepository.updateQuantity(positionLot.id(), newPositionLotQuantity);
        }
        return fifoCostBasis;
    }

    private Position requirePosition(Optional<Position> optionalPosition, String asset) {
        return optionalPosition.orElseThrow(() ->
                new TransactionException("Position does not exist for sell order. Asset: %s".formatted(asset)));
    }

    private void requireSufficientQuantity(Position position, TransactionDto transaction) {
        if (position.quantity() < transaction.quantity()) {
            throw new TransactionException("Existing position is too small for sell order. Position qty: %s, transaction qty: %s"
                    .formatted(position.quantity(), transaction.quantity()));
        }
    }
}
