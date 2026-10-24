package edu.cibertec.products.controller;

import edu.cibertec.products.config.SecurityConfig;
import edu.cibertec.products.config.WebConfig;
import edu.cibertec.products.dto.PageResponse;
import edu.cibertec.products.dto.ProductResponse;
import edu.cibertec.products.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StoreCatalogController.class)
@Import({SecurityConfig.class, WebConfig.class})
class StoreCatalogControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean ProductService products;

    private ProductResponse book(boolean active) {
        return new ProductResponse(1L, "Libro", "9781234567897", "Autor", 1L, "Editorial", 1L,
                "Literatura", 2026, "Descripción", null, new BigDecimal("10.00"),
                new BigDecimal("50.00"), new BigDecimal("15.00"), active, 3, true, null, null, null);
    }

    @Test
    void returnsOnlyPublicCatalogFieldsAndRequestsActiveBooks() throws Exception {
        when(products.findAll(any(Pageable.class), isNull(), isNull(), eq(true)))
                .thenReturn(new PageResponse<>(List.of(book(true)), 0, 20, 1, 1, true, true));
        mvc.perform(get("/api/products/catalog"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].name").value("Libro"))
                .andExpect(jsonPath("$.content[0].salePrice").value(15))
                .andExpect(jsonPath("$.content[0].purchasePrice").doesNotExist())
                .andExpect(jsonPath("$.content[0].profitMargin").doesNotExist())
                .andExpect(jsonPath("$.content[0].lastMovement").doesNotExist());
        verify(products).findAll(any(Pageable.class), isNull(), isNull(), eq(true));
    }

    @Test
    void detailHidesInternalCostsAndRejectsInactiveBooks() throws Exception {
        when(products.findById(1L)).thenReturn(book(true));
        mvc.perform(get("/api/products/catalog/1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.isbn").value("9781234567897"))
                .andExpect(jsonPath("$.purchasePrice").doesNotExist())
                .andExpect(jsonPath("$.profitMargin").doesNotExist());
        when(products.findById(1L)).thenReturn(book(false));
        mvc.perform(get("/api/products/catalog/1")).andExpect(status().isNotFound());
    }
}
