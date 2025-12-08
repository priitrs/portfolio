package ee.bank.portfolio.service;

import ee.bank.portfolio.exception.TransactionException;
import ee.bank.portfolio.model.Position;
import ee.bank.portfolio.model.Transaction;
import ee.bank.portfolio.repository.PositionLotRepository;
import ee.bank.portfolio.repository.PositionRepository;
import ee.bank.portfolio.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    public static final String DEFAULT_ASSET = "ASSET_1";
    private static final String BUY = "buy";
    private static final String SELL = "sell";
    private final TransactionRepository transactionRepository;
    private final PositionRepository positionRepository;
    private final PositionLotRepository positionLotRepository;

    public TransactionService(TransactionRepository transactionRepository, PositionRepository positionRepository, PositionLotRepository positionLotRepository) {
        this.transactionRepository = transactionRepository;
        this.positionRepository = positionRepository;
        this.positionLotRepository = positionLotRepository;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.getAll();
    }

    @Transactional
    public void handleAddTransaction(Transaction transaction) {
        var optionalPosition = positionRepository.getByAsset(DEFAULT_ASSET);
        if (BUY.equals(transaction.type())) {
            handleBuy(transaction, optionalPosition);
        } else if (SELL.equals(transaction.type())) {
            handleSell(transaction, optionalPosition);
        }
        transactionRepository.save(transaction);
    }

    private void handleBuy(Transaction transaction, Optional<Position> optionalPosition) {
        positionLotRepository.insert(DEFAULT_ASSET, transaction.quantity(), transaction.getBuyAverageCost());
        if (optionalPosition.isEmpty()) {
            positionRepository.insert(new Position(DEFAULT_ASSET, transaction.quantity(), transaction.getBuyAverageCost(), transaction.getBuyTotalCost(), BigDecimal.ZERO));
        } else {
            positionRepository.update(getUpdatedPositionForBuy(transaction, optionalPosition.get()));
        }
    }

    private void handleSell(Transaction transaction, Optional<Position> optionalPosition) {
        var position = requirePosition(optionalPosition);
        requireSufficientQuantity(position, transaction);
        var fifoCostBasis = processPositionLotsForFifoCostBasis(transaction);
        positionRepository.update(getUpdatedPositionForSell(transaction, position, fifoCostBasis));
    }

    private Position getUpdatedPositionForBuy(Transaction transaction, Position position) {
        int newQuantity = position.quantity() + transaction.quantity();
        var newTotalCost = position.totalCost().add(transaction.getBuyTotalCost());
        var newAverageCost = newTotalCost.divide(BigDecimal.valueOf(newQuantity), 6, RoundingMode.HALF_UP);

        return new Position(
                position.asset(),
                newQuantity,
                newAverageCost,
                newTotalCost,
                position.realizedProfitLoss()
        );
    }

    private Position getUpdatedPositionForSell(Transaction transaction, Position position, BigDecimal fifoCostBasis) {
        int remainingPositionQuantity = position.quantity() - transaction.quantity();
        var remainingTotalCost = position.totalCost().subtract(fifoCostBasis);
        var updatedAverageCost = remainingPositionQuantity > 0 ?
                remainingTotalCost.divide(BigDecimal.valueOf(remainingPositionQuantity), 6, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        var updatedRealizedProfitLoss = position.realizedProfitLoss().add(transaction.getSellProceeds()).subtract(fifoCostBasis);

        return new Position(
                position.asset(),
                remainingPositionQuantity,
                updatedAverageCost,
                remainingTotalCost,
                updatedRealizedProfitLoss
        );
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

    private Position requirePosition(Optional<Position> optionalPosition) {
        return optionalPosition.orElseThrow(() ->
                new TransactionException("Position does not exist for sell order. Asset: %s".formatted(DEFAULT_ASSET)));
    }

    private void requireSufficientQuantity(Position position, Transaction transaction) {
        if (position.quantity() < transaction.quantity()) {
            throw new TransactionException("Existing position is too small for sell order. Position qty: %s, transaction qty: %s"
                    .formatted(position.quantity(), transaction.quantity()));
        }
    }
}
