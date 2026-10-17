package edu.cibertec.products.service.impl;

import edu.cibertec.products.domain.entity.Product;
import edu.cibertec.products.domain.entity.StockMovement;
import edu.cibertec.products.domain.entity.StockMovementType;
import edu.cibertec.products.dto.StockMovementRequest;
import edu.cibertec.products.dto.StockMovementResponse;
import edu.cibertec.products.exception.ResourceNotFoundException;
import edu.cibertec.products.exception.InsufficientStockException;
import edu.cibertec.products.repository.ProductRepository;
import edu.cibertec.products.repository.StockMovementRepository;
import edu.cibertec.products.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockMovementServiceImpl implements StockMovementService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional
    public StockMovementResponse create(Long productId, StockMovementRequest request) {
        productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        LocalDateTime now = LocalDateTime.now();
        boolean stockUpdated = request.type() == StockMovementType.ENTRY
                ? productRepository.incrementStock(productId, request.quantity(), now)
                : productRepository.decrementStockIfAvailable(productId, request.quantity(), now);
        if (!stockUpdated) {
            if (request.type() == StockMovementType.EXIT) {
                throw new InsufficientStockException("Insufficient stock for this exit");
            }
            throw new IllegalStateException("Stock could not be updated");
        }

        Integer newStock = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"))
                .getStock();
        if (newStock == null) {
            throw new IllegalStateException("Product stock cannot be null");
        }
        StockMovement movement = StockMovement.builder()
                .idProduct(productId)
                .type(request.type())
                .quantity(request.quantity())
                .movementDate(request.movementDate() == null ? now : request.movementDate())
                .resultingStock(newStock)
                .notes(request.notes() == null ? null : request.notes().trim())
                .createdAt(now)
                .build();
        return toResponse(stockMovementRepository.save(movement));
    }

    @Override
    public List<StockMovementResponse> findByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found");
        }
        return stockMovementRepository.findByIdProductOrderByMovementDateDescIdMovementDesc(productId)
                .stream().map(this::toResponse).toList();
    }

    private StockMovementResponse toResponse(StockMovement movement) {
        return new StockMovementResponse(movement.getIdMovement(), movement.getIdProduct(), movement.getType(),
                movement.getQuantity(), movement.getMovementDate(), movement.getResultingStock(), movement.getNotes(),
                movement.getCreatedAt());
    }
}
