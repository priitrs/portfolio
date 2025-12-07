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

    private static final String ASSET_1 = "ASSET_1";
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

    public List<Transaction> getAllTransactions(){
        return transactionRepository.getAll();
    }

    public void handleAddTransaction(Transaction transaction) {
        Optional<Position> optionalPosition = positionRepository.getByAsset(ASSET_1);

        transactionRepository.save(transaction);
        if (BUY.equals(transaction.type())){
            positionLotRepository.insert(ASSET_1, transaction.quantity(), transaction.getAverageCost());

            if (optionalPosition.isEmpty()) {
                positionRepository.insert(new Position(ASSET_1, transaction.quantity(), transaction.getAverageCost(), transaction.getTotalCost(), BigDecimal.ZERO));
            } else {
                positionRepository.update(getUpdatedPosition(transaction, optionalPosition.get()));
            }
        }
    }

    private Position getUpdatedPosition(Transaction transaction, Position existingPosition) {
        int newQuantity = existingPosition.quantity() + transaction.quantity();
        var newTotalCost = existingPosition.totalCost().add(transaction.getTotalCost());
        var newAverageCost = newTotalCost.divide(BigDecimal.valueOf(newQuantity), 6, RoundingMode.HALF_UP);
        return new Position(
                existingPosition.asset(),
                newQuantity,
                newAverageCost,
                newTotalCost,
                existingPosition.realizedProfitLoss());
    }
}
