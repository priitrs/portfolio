package ee.bank.portfolio.service;

import ee.bank.portfolio.model.Position;
import ee.bank.portfolio.model.PositionLot;
import ee.bank.portfolio.model.Transaction;
import ee.bank.portfolio.model.TransactionDto;
import ee.bank.portfolio.repository.PositionLotRepository;
import ee.bank.portfolio.repository.PositionRepository;
import ee.bank.portfolio.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static ee.bank.portfolio.service.TransactionService.BUY;
import static ee.bank.portfolio.service.TransactionService.SELL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock PositionRepository positionRepository;
    @Mock PositionLotRepository positionLotRepository;
    @InjectMocks TransactionService service;

    @Captor ArgumentCaptor<Transaction> transactionCaptor;
    @Captor ArgumentCaptor<PositionLot> lotCaptor;
    @Captor ArgumentCaptor<Position> positionCaptor;

    private final Instant now = Instant.parse("2024-01-01T10:00:00Z");
    private final TransactionDto transactionDto = new TransactionDto("ASSET", now, BUY, 1, BigDecimal.TEN, BigDecimal.TWO);

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(transactionRepository, positionRepository, positionLotRepository);
    }

    @Test
    void handleAddTransaction_buy_noPosition() {
        Transaction transactionEntity = new Transaction(UUID.randomUUID(), "ASSET", now, BUY, 1, BigDecimal.TEN, BigDecimal.TWO);
        when(positionRepository.findFirstByAsset(any())).thenReturn(Optional.empty());
        when(transactionRepository.save(any())).thenReturn(transactionEntity);

        service.handleAddTransaction(transactionDto);

        verify(positionRepository).findFirstByAsset("ASSET");
        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction actualTransaction = transactionCaptor.getValue();
        assertEquals("ASSET", actualTransaction.getAsset());
        assertEquals(now, actualTransaction.getTimestamp());
        assertEquals(BUY, actualTransaction.getType());
        assertEquals(1, actualTransaction.getQuantity());
        assertEquals(0, actualTransaction.getPrice().compareTo(BigDecimal.TEN));
        assertEquals(0, actualTransaction.getFee().compareTo(BigDecimal.TWO));
        verify(positionLotRepository).save(lotCaptor.capture());
        PositionLot positionLot = lotCaptor.getValue();
        assertEquals("ASSET", positionLot.getAsset());
        assertEquals(1, positionLot.getQtyRemaining());
        assertEquals(transactionEntity.getBuyAverageCost(), positionLot.getUnitCost());
        verify(positionRepository).save(positionCaptor.capture());
        Position position = positionCaptor.getValue();
        assertEquals("ASSET", position.getAsset());
        assertEquals(1, position.getQuantity());
        assertEquals(0, BigDecimal.valueOf(12).compareTo(position.getAverageCost()));
        assertEquals(0, BigDecimal.valueOf(12).compareTo(position.getTotalCost()));
        assertEquals(0, BigDecimal.ZERO.compareTo(position.getRealizedProfitLoss()));
    }


    @Test
    void handleAddTransaction_buy_existingPosition() {
        Position existingPosition = new Position("ASSET", 2, BigDecimal.valueOf(15), BigDecimal.valueOf(30), BigDecimal.ZERO);
        Transaction transactionEntity = new Transaction(UUID.randomUUID(), "ASSET", now, BUY, 1, BigDecimal.TEN, BigDecimal.TWO);
        when(positionRepository.findFirstByAsset(any())).thenReturn(Optional.of(existingPosition));
        when(transactionRepository.save(any())).thenReturn(transactionEntity);

        service.handleAddTransaction(transactionDto);

        verify(positionRepository).findFirstByAsset("ASSET");
        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction actualTransaction = transactionCaptor.getValue();
        assertEquals("ASSET", actualTransaction.getAsset());
        assertEquals(now, actualTransaction.getTimestamp());
        assertEquals(BUY, actualTransaction.getType());
        assertEquals(1, actualTransaction.getQuantity());
        assertEquals(0, actualTransaction.getPrice().compareTo(BigDecimal.TEN));
        assertEquals(0, actualTransaction.getFee().compareTo(BigDecimal.TWO));
        verify(positionLotRepository).save(lotCaptor.capture());
        PositionLot positionLot = lotCaptor.getValue();
        assertEquals("ASSET", positionLot.getAsset());
        assertEquals(1, positionLot.getQtyRemaining());
        assertEquals(transactionEntity.getBuyAverageCost(), positionLot.getUnitCost());
        verify(positionRepository).save(positionCaptor.capture());
        Position position = positionCaptor.getValue();
        assertEquals("ASSET", position.getAsset());
        assertEquals(3, position.getQuantity());
        assertEquals(BigDecimal.valueOf(14), position.getAverageCost());
        assertEquals(BigDecimal.valueOf(42), position.getTotalCost());
        assertEquals(BigDecimal.ZERO, position.getRealizedProfitLoss());
    }

    @Test
    void handleAddTransaction_sell() {
        TransactionDto transactionDtoSell = new TransactionDto("ASSET", now, SELL, 1, BigDecimal.valueOf(15), BigDecimal.ONE);
        Position existingPosition = new Position("ASSET", 3, BigDecimal.valueOf(14), BigDecimal.valueOf(42), BigDecimal.ZERO);
        Transaction transactionEntity = new Transaction(UUID.randomUUID(), "ASSET", now, SELL, 1, BigDecimal.valueOf(15), BigDecimal.ONE);
        when(positionRepository.findFirstByAsset(any())).thenReturn(Optional.of(existingPosition));
        when(transactionRepository.save(any())).thenReturn(transactionEntity);
        when(positionLotRepository.findFirstByAssetAndQtyRemainingGreaterThanOrderByIdAsc(any(), anyInt())).thenReturn(Optional.of(new PositionLot(null, "ASSET", 1, BigDecimal.valueOf(12))));

        service.handleAddTransaction(transactionDtoSell);

        verify(positionRepository).findFirstByAsset("ASSET");
        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction actualTransaction = transactionCaptor.getValue();
        assertEquals("ASSET", actualTransaction.getAsset());
        assertEquals(now, actualTransaction.getTimestamp());
        assertEquals(SELL, actualTransaction.getType());
        assertEquals(1, actualTransaction.getQuantity());
        assertEquals(0, actualTransaction.getPrice().compareTo(BigDecimal.valueOf(15)));
        assertEquals(0, actualTransaction.getFee().compareTo(BigDecimal.ONE));
        verify(positionLotRepository).findFirstByAssetAndQtyRemainingGreaterThanOrderByIdAsc("ASSET", 0);
        verify(positionLotRepository).save(lotCaptor.capture());
        PositionLot positionLot = lotCaptor.getValue();
        assertEquals("ASSET", positionLot.getAsset());
        assertEquals(0, positionLot.getQtyRemaining());
        assertEquals(BigDecimal.valueOf(12), positionLot.getUnitCost());
        verify(positionRepository).save(positionCaptor.capture());
        Position position = positionCaptor.getValue();
        assertEquals("ASSET", position.getAsset());
        assertEquals(2, position.getQuantity());
        assertEquals(BigDecimal.valueOf(15), position.getAverageCost());
        assertEquals(BigDecimal.valueOf(30), position.getTotalCost());
        assertEquals(BigDecimal.valueOf(2), position.getRealizedProfitLoss());
    }
}