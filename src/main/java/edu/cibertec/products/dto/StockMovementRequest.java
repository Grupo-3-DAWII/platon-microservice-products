package edu.cibertec.products.dto;

import edu.cibertec.products.domain.entity.StockMovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record StockMovementRequest(
        @NotNull StockMovementType type,
        @NotNull @Min(1) Integer quantity,
        LocalDateTime movementDate,
        @Size(max = 500) String notes
) {
}
