package edu.cibertec.products.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 20) String isbn,
        @NotBlank @Size(max = 160) String author,
        @NotNull Long editorialId,
        @NotNull Long genreId,
        @NotNull @Min(1) @Max(3000) Integer publicationYear,
        @NotBlank @Size(max = 2000) String description,
        byte[] image,
        @Size(max = 100) String imageContentType,
        @NotNull @DecimalMin("0.00") BigDecimal purchasePrice,
        @NotNull @DecimalMin("0.00") BigDecimal profitMargin,
        Boolean active,
        @NotNull @Min(0) Integer stock
) {
}
