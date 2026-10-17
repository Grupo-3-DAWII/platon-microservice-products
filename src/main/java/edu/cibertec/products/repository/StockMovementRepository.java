package edu.cibertec.products.repository;

import edu.cibertec.products.domain.entity.StockMovement;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface StockMovementRepository extends ListCrudRepository<StockMovement, Long> {

    List<StockMovement> findByIdProductOrderByMovementDateDescIdMovementDesc(Long productId);

    Optional<StockMovement> findFirstByIdProductOrderByMovementDateDescIdMovementDesc(Long productId);
}
