package ee.bank.portfolio.service;

import ee.bank.portfolio.exception.TransactionException;
import ee.bank.portfolio.model.Position;
import ee.bank.portfolio.model.PositionLot;
import ee.bank.portfolio.model.Transaction;
import ee.bank.portfolio.model.TransactionDto;
import ee.bank.portfolio.repository.PositionLotRepository;
import ee.bank.portfolio.repository.PositionRepository;
import ee.bank.portfolio.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static ee.bank.portfolio.config.MathContexts.FINANCE;

@Service
@AllArgsConstructor
public class TransactionService {

    static final String BUY = "buy";
    static final String SELL = "sell";
    private final TransactionRepository transactionRepository;
    private final PositionRepository positionRepository;
    private final PositionLotRepository positionLotRepository;

    public List<TransactionDto> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(Transaction::toDto)
                .toList();
    }

    @Transactional
    public void handleAddTransaction(TransactionDto transactionDto) {
        var optionalPosition = positionRepository.findFirstByAsset(transactionDto.asset());
        switch (transactionDto.type()) {
            case BUY -> handleBuy(transactionDto, optionalPosition);
            case SELL -> handleSell(transactionDto, optionalPosition);
            default -> throw new TransactionException(
                    "Invalid transaction type: %s".formatted(transactionDto.type())
            );
        }
    }

    private void handleBuy(TransactionDto transactionDto, Optional<Position> optionalPosition) {
        var transaction = transactionRepository.save(getEntity(transactionDto));
        positionLotRepository.save(new PositionLot(null, transaction.getAsset(), transaction.getQuantity(), transaction.getBuyAverageCost()));
        positionRepository.save(getPosition(optionalPosition, transaction));
    }

    private Position getPosition(Optional<Position> optionalPosition, Transaction transaction) {
        return optionalPosition
                .map(position -> getUpdatedPositionForBuy(transaction, position))
                .orElseGet(() -> new Position(
                        transaction.getAsset(),
                        transaction.getQuantity(),
                        transaction.getBuyAverageCost(),
                        transaction.getBuyTotalCost(),
                        BigDecimal.ZERO
                ));
    }

    private Position getUpdatedPositionForBuy(Transaction transaction, Position position) {
        int newPositionQuantity = position.getQuantity() + transaction.getQuantity();
        var newPositionTotalCost = position.getTotalCost().add(transaction.getBuyTotalCost(), FINANCE);
        var newAverageCost = newPositionTotalCost.divide(BigDecimal.valueOf(newPositionQuantity), FINANCE);

        return position
                .withQuantity(newPositionQuantity)
                .withAverageCost(newAverageCost)
                .withTotalCost(newPositionTotalCost);
    }

    private void handleSell(TransactionDto transactionDto, Optional<Position> optionalPosition) {
        var position = requirePosition(optionalPosition, transactionDto.asset());
        requireSufficientQuantity(position, transactionDto);
        var transaction = transactionRepository.save(getEntity(transactionDto));
        var fifoCostBasis = processPositionLotsForFifoCostBasis(transaction);
        positionRepository.save(getUpdatedPositionForSell(transaction, position, fifoCostBasis));
    }

    private Position getUpdatedPositionForSell(Transaction transaction, Position position, BigDecimal fifoCostBasis) {
        int remainingPositionQuantity = position.getQuantity() - transaction.getQuantity();
        var remainingTotalCost = position.getTotalCost().subtract(fifoCostBasis, FINANCE);

        var updatedAverageCost = remainingPositionQuantity > 0 ?
                remainingTotalCost.divide(BigDecimal.valueOf(remainingPositionQuantity), FINANCE) :
                BigDecimal.ZERO;

        var updatedRealizedProfitLoss = position.getRealizedProfitLoss()
                .add(transaction.getSellProceeds())
                .subtract(fifoCostBasis, FINANCE);

        return position
                .withQuantity(remainingPositionQuantity)
                .withAverageCost(updatedAverageCost)
                .withTotalCost(remainingTotalCost)
                .withRealizedProfitLoss(updatedRealizedProfitLoss);
    }

    private BigDecimal processPositionLotsForFifoCostBasis(Transaction transaction) {
        int remainingTransactionQuantity = transaction.getQuantity();
        var fifoCostBasis = BigDecimal.ZERO;
        while (remainingTransactionQuantity > 0) {
            int newPositionLotQuantity;
            BigDecimal additionToFifoCostBasis;
            var positionLot = positionLotRepository.findFirstByAssetAndQtyRemainingGreaterThanOrderByIdAsc(transaction.getAsset(), 0).orElseThrow();
            if (remainingTransactionQuantity > positionLot.getQtyRemaining()) {
                additionToFifoCostBasis = positionLot.getUnitCost().multiply(BigDecimal.valueOf(positionLot.getQtyRemaining()), FINANCE);
                remainingTransactionQuantity -= positionLot.getQtyRemaining();
                newPositionLotQuantity = 0;
            } else {
                additionToFifoCostBasis = positionLot.getUnitCost().multiply(BigDecimal.valueOf(remainingTransactionQuantity), FINANCE);
                newPositionLotQuantity = positionLot.getQtyRemaining() - remainingTransactionQuantity;
                remainingTransactionQuantity = 0;
            }
            fifoCostBasis = fifoCostBasis.add(additionToFifoCostBasis, FINANCE);
            positionLotRepository.save(positionLot.withQtyRemaining(newPositionLotQuantity));
        }
        return fifoCostBasis;
    }

    private Position requirePosition(Optional<Position> optionalPosition, String asset) {
        return optionalPosition.orElseThrow(() ->
                new TransactionException("Position does not exist for sell order. Asset: %s".formatted(asset)));
    }

    private void requireSufficientQuantity(Position position, TransactionDto transaction) {
        if (position.getQuantity() < transaction.quantity()) {
            throw new TransactionException("Existing position is too small for sell order. Position qty: %s, transaction qty: %s"
                    .formatted(position.getQuantity(), transaction.quantity()));
        }
    }

    private Transaction getEntity(TransactionDto transactionDto) {
        return new Transaction(
                null,
                transactionDto.asset(),
                transactionDto.timestamp(),
                transactionDto.type(),
                transactionDto.quantity(),
                transactionDto.price(),
                transactionDto.fee()
        );
    }
}
