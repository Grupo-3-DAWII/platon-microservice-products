package edu.cibertec.products.dto;

import edu.cibertec.products.domain.entity.StockMovementType;

import java.time.LocalDateTime;

public record StockMovementResponse(
        Long idMovement,
        Long productId,
        StockMovementType type,
        Integer quantity,
        LocalDateTime movementDate,
        Integer resultingStock,
        String notes,
        LocalDateTime createdAt
) {
}
