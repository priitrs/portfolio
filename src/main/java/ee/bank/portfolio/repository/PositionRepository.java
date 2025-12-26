package ee.bank.portfolio.repository;

import ee.bank.portfolio.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, String> {

    Optional<Position> findFirstByAsset(String asset);
}
