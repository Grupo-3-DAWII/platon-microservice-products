package edu.cibertec.products.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long idProduct,
        String name,
        String isbn,
        String author,
        Long editorialId,
        String editorial,
        Long genreId,
        String genre,
        Integer publicationYear,
        String description,
        String image,
        BigDecimal purchasePrice,
        BigDecimal profitMargin,
        BigDecimal salePrice,
        Boolean active,
        Integer stock,
        boolean available,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        StockMovementResponse lastMovement
) {
}
