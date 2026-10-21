package edu.cibertec.products.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductDetailResponse(
        String name,
        String isbn,
        String author,
        String editorial,
        String genre,
        Integer publicationYear,
        String description,
        String image,
        BigDecimal purchasePrice,
        BigDecimal salePrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean active
) {
}
