package edu.cibertec.products.controller;

import edu.cibertec.products.dto.PageResponse;
import edu.cibertec.products.dto.ProductResponse;
import edu.cibertec.products.exception.ResourceNotFoundException;
import edu.cibertec.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products/catalog")
@RequiredArgsConstructor
public class StoreCatalogController {
    private final ProductService products;

    @GetMapping
    public PageResponse<BookResponse> list(@PageableDefault(size = 20, sort = "idProduct") Pageable pageable,
                                           @RequestParam(required = false) String search) {
        var page = products.findAll(pageable, search, null, true);
        return new PageResponse<>(page.content().stream().map(BookResponse::from).toList(),
                page.page(), page.size(), page.totalElements(), page.totalPages(), page.first(), page.last());
    }

    @GetMapping("/{id}")
    public BookResponse detail(@PathVariable Long id) {
        var product = products.findById(id);
        if (!Boolean.TRUE.equals(product.active())) throw new ResourceNotFoundException("Libro no disponible");
        return BookResponse.from(product);
    }

    public record BookResponse(Long idProduct, String name, String isbn, String author, String editorial,
                               String genre, Integer publicationYear, String description, String image,
                               BigDecimal salePrice, Boolean active, Integer stock, boolean available) {
        static BookResponse from(ProductResponse product) {
            return new BookResponse(product.idProduct(), product.name(), product.isbn(), product.author(),
                    product.editorial(), product.genre(), product.publicationYear(), product.description(),
                    product.image(), product.salePrice(), product.active(), product.stock(), product.available());
        }
    }
}
