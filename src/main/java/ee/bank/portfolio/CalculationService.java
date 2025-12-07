package ee.bank.portfolio;

import ee.bank.portfolio.positionlots.PositionLotRepository;
import ee.bank.portfolio.positions.Position;
import ee.bank.portfolio.positions.PositionRepository;
import ee.bank.portfolio.transactions.Transaction;
import ee.bank.portfolio.transactions.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class CalculationService {

    public static final String ASSET_1 = "ASSET_1";
    private static final String BUY = "buy";
    private static final String SELL = "sell";
    private final TransactionRepository transactionRepository;
    private final PositionRepository positionRepository;
    private final PositionLotRepository positionLotRepository;

    public CalculationService(TransactionRepository transactionRepository, PositionRepository positionRepository, PositionLotRepository positionLotRepository) {
        this.transactionRepository = transactionRepository;
        this.positionRepository = positionRepository;
        this.positionLotRepository = positionLotRepository;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.getAll();
    }

    public void handleAddTransaction(Transaction transaction) {
        Optional<Position> optionalPosition = positionRepository.getByAsset(ASSET_1);

        if (BUY.equals(transaction.type())) {
            positionLotRepository.insert(ASSET_1, transaction.quantity(), transaction.getBuyAverageCost());
            if (optionalPosition.isEmpty()) {
                positionRepository.insert(new Position(ASSET_1, transaction.quantity(), transaction.getBuyAverageCost(), transaction.getBuyTotalCost(), BigDecimal.ZERO));
            } else {
                positionRepository.update(getUpdatedPositionForBuy(transaction, optionalPosition.get()));
            }
        }

        if (SELL.equals(transaction.type())) {
            var position = requirePosition(optionalPosition);
            requireSufficientQuantity(position, transaction);
            var fifoCostBasis = getFifoCostBasis(transaction);
            positionRepository.update(getUpdatedPositionForSell(transaction, optionalPosition.get(), fifoCostBasis));
        }

        transactionRepository.save(transaction);
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
                position.realizedProfitLoss());
    }

    private Position getUpdatedPositionForSell(Transaction transaction, Position position, BigDecimal fifoCostBasis) {
        int remainingPositionQuantity = position.quantity() - transaction.quantity();
        var remainingTotalCost = position.totalCost().subtract(fifoCostBasis);
        var updatedAverageCost = remainingPositionQuantity > 0 ?
                remainingTotalCost.divide(BigDecimal.valueOf(remainingPositionQuantity), 6, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        var updatedRealizedProfitLoss = position.realizedProfitLoss().add(transaction.getSellProceeds().subtract(fifoCostBasis));

        return new Position(
                position.asset(),
                remainingPositionQuantity,
                remainingTotalCost,
                updatedAverageCost,
                updatedRealizedProfitLoss
        );
    }

    private BigDecimal getFifoCostBasis(Transaction transaction) {
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
                remainingTransactionQuantity = 0;
                newPositionLotQuantity = positionLot.qtyRemaining() - remainingTransactionQuantity;
            }
            positionLotRepository.updateQuantity(positionLot.id(), newPositionLotQuantity);
        }
        return fifoCostBasis;
    }

    private Position requirePosition(Optional<Position> optionalPosition) {
        return optionalPosition.orElseThrow(() ->
                new TransactionException("Position does not exist for sell order. Asset: " + ASSET_1));
    }

    private void requireSufficientQuantity(Position position, Transaction transaction) {
        if (position.quantity() < transaction.quantity()) {
            throw new TransactionException("Existing position is too small for sell order. Position qty: %s, transaction qty: %s"
                    .formatted(position.quantity(), transaction.quantity()));
        }
    }
}
