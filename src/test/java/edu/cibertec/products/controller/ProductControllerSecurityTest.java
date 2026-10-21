package edu.cibertec.products.controller;

import edu.cibertec.products.config.SecurityConfig;
import edu.cibertec.products.config.WebConfig;
import edu.cibertec.products.dto.PageResponse;
import edu.cibertec.products.dto.ProductResponse;

import edu.cibertec.products.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, WebConfig.class})
class ProductControllerSecurityTest {

    @Test
    void shouldUpdateDetailAndValidateBody() throws Exception {
        var detail = new edu.cibertec.products.dto.ProductDetailResponse(
                "Updated", "978-612-00-0001-1", "Miguel de Cervantes", "Alfaguara", "Novela",
                1605, "Descripcion", null, new java.math.BigDecimal("80.00"),
                new java.math.BigDecimal("128.00"), null, null, true);
        when(productService.updateDetail(org.mockito.ArgumentMatchers.eq(1L), any())).thenReturn(detail);
        String body = """
                {"name":"Updated","isbn":"978-612-00-0001-1","author":"Miguel de Cervantes",
                 "editorialId":1,"genreId":1,"publicationYear":1605,"description":"Descripcion",
                 "image":null,"purchasePrice":80.00,"profitMargin":60.00,"active":true}
                """;

        mockMvc.perform(put("/api/v1/products/1/detail").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.name").value("Updated"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.*", org.hamcrest.Matchers.hasSize(13)));
        org.mockito.Mockito.verify(productService).updateDetail(org.mockito.ArgumentMatchers.eq(1L), any());
        mockMvc.perform(put("/api/v1/products/1/detail").contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/products/1/detail").with(httpBasic("student", "student123"))
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnOnlyDetailFieldsByProductId() throws Exception {
        var product = response();
        when(productService.findDetailById(1L)).thenReturn(
                new edu.cibertec.products.dto.ProductDetailResponse(
                        product.name(), product.isbn(), product.author(), product.editorial(), product.genre(),
                        product.publicationYear(), product.description(), product.image(),
                        product.purchasePrice(), product.salePrice(), product.createdAt(), product.updatedAt(),
                        product.active()));

        mockMvc.perform(get("/api/v1/products/1/detail"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.name").value(product.name()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.*", org.hamcrest.Matchers.hasSize(13)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.stock").doesNotExist())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.lastMovement").doesNotExist());
        org.mockito.Mockito.verify(productService).findDetailById(1L);
    }

    @Test
    void shouldReturnNotFoundForMissingProductDetail() throws Exception {
        when(productService.findDetailById(999L))
                .thenThrow(new edu.cibertec.products.exception.ResourceNotFoundException("Product not found"));

        mockMvc.perform(get("/api/v1/products/999/detail"))
                .andExpect(status().isNotFound());
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void shouldRejectAnonymousRead() throws Exception {
        mockMvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAnonymousV1ReadAndDelete() throws Exception {
        when(productService.findAll(any(Pageable.class), org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull())).thenReturn(page());
        mockMvc.perform(get("/api/v1/products")).andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.content[0].idProduct").value(1));
        mockMvc.perform(delete("/api/v1/products/1")).andExpect(status().isNoContent());
        org.mockito.Mockito.verify(productService).delete(1L);
        mockMvc.perform(delete("/api/products/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowUserToReadProducts() throws Exception {
        when(productService.findAll(any(Pageable.class), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull()))
                .thenReturn(page());
        when(productService.findById(1L)).thenReturn(response());

        mockMvc.perform(get("/api/products").with(httpBasic("student", "student123")))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.content[0].isbn").value("978-612-00-0001-1"));
        mockMvc.perform(get("/api/products/1").with(httpBasic("student", "student123")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAdminToWriteProducts() throws Exception {
        String body = "{\"name\":\"Don Quijote\",\"isbn\":\"978-612-00-0001-1\",\"author\":\"Miguel de Cervantes\","
                + "\"editorialId\":1,\"genreId\":1,\"publicationYear\":1605,"
                + "\"description\":\"Las aventuras del ingenioso hidalgo.\","
                + "\"image\":\"https://example.com/book.png\",\"purchasePrice\":80.00,\"profitMargin\":60.00,\"active\":true}";
        when(productService.create(any())).thenReturn(response());
        when(productService.update(any(), any())).thenReturn(response());
        doNothing().when(productService).delete(1L);

        mockMvc.perform(post("/api/products").with(httpBasic("admin", "admin123"))
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/products/1").with(httpBasic("admin", "admin123"))
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/products/1").with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectUserWriteAndInvalidAdminRequest() throws Exception {
        mockMvc.perform(post("/api/products").with(httpBasic("student", "student123"))
                        .contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/products").with(httpBasic("admin", "admin123"))
                        .contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFilterOutOfStockActiveProducts() throws Exception {
        when(productService.findAll(any(Pageable.class), org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq(0), org.mockito.ArgumentMatchers.eq(true))).thenReturn(page());
        mockMvc.perform(get("/api/products?maxStock=0&active=true").with(httpBasic("student", "student123")))
                .andExpect(status().isOk());
        org.mockito.Mockito.verify(productService).findAll(any(Pageable.class), org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq(0), org.mockito.ArgumentMatchers.eq(true));
    }

    @Test
    void shouldRejectNegativeStockFilter() throws Exception {
        mockMvc.perform(get("/api/products?maxStock=-1").with(httpBasic("student", "student123")))
                .andExpect(status().isBadRequest());
    }
    private PageResponse<ProductResponse> page() {
        return new PageResponse<>(List.of(response()), 0, 20, 1, 1, true, true);
    }

    private ProductResponse response() {
        return new ProductResponse(1L, "Don Quijote", "978-612-00-0001-1", "Miguel de Cervantes",
                1L, "Alfaguara", 1L, "Novela", 1605, "Las aventuras del ingenioso hidalgo.",
                "https://example.com/book.png", new java.math.BigDecimal("80.00"), new java.math.BigDecimal("60.00"),
                new java.math.BigDecimal("128.00"), true, 5, true, null, null, null);
    }
}
