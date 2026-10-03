package edu.cibertec.products.controller;

import edu.cibertec.products.config.SecurityConfig;
import edu.cibertec.products.config.WebConfig;
import edu.cibertec.products.dto.PageResponse;
import edu.cibertec.products.dto.ProductResponse;
import edu.cibertec.products.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void shouldRejectAnonymousRead() throws Exception {
        mockMvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowUserToReadProducts() throws Exception {
        when(productService.findAll(any(Pageable.class), org.mockito.ArgumentMatchers.isNull()))
                .thenReturn(page());
        when(productService.findById(1L)).thenReturn(response());

        mockMvc.perform(get("/api/products").with(httpBasic("student", "student123")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/products/1").with(httpBasic("student", "student123")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAdminToWriteProducts() throws Exception {
        String body = "{\"name\":\"Don Quijote\",\"stock\":5}";
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

    private PageResponse<ProductResponse> page() {
        return new PageResponse<>(List.of(response()), 0, 20, 1, 1, true, true);
    }

    private ProductResponse response() {
        return new ProductResponse(1L, "Don Quijote", 5, true);
    }
}
