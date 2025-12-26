package ee.bank.portfolio.repository;

import ee.bank.portfolio.model.PositionLot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PositionLotRepository extends JpaRepository<PositionLot, Long> {

    List<PositionLot> findByAssetOrderByIdAsc(String asset);

    Optional<PositionLot> findFirstByAssetAndQtyRemainingGreaterThanOrderByIdAsc(String asset, int qty);
}
