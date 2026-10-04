package edu.cibertec.products.dto;

import java.math.BigDecimal;

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
        byte[] image,
        String imageContentType,
        BigDecimal purchasePrice,
        BigDecimal profitMargin,
        BigDecimal salePrice,
        Boolean active,
        Integer stock,
        boolean available
) {
}
