package edu.cibertec.products.dto;

import java.math.BigDecimal;

public record ProductSummaryResponse(
        Long idProduct,
        String name,
        String author,
        String editorial,
        String genre,
        String image,
        BigDecimal salePrice,
        Integer stock,
        Boolean active,
        boolean available
) {
}
