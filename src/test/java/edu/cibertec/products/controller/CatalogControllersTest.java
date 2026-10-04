package edu.cibertec.products.controller;

import edu.cibertec.products.config.SecurityConfig;
import edu.cibertec.products.config.WebConfig;
import edu.cibertec.products.dto.CatalogResponse;
import edu.cibertec.products.service.EditorialService;
import edu.cibertec.products.service.GenreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({EditorialController.class, GenreController.class})
@Import({SecurityConfig.class, WebConfig.class})
class CatalogControllersTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EditorialService editorialService;

    @MockBean
    private GenreService genreService;

    @Test
    void shouldReturnEditorialsAndGenres() throws Exception {
        when(editorialService.findAll()).thenReturn(List.of(new CatalogResponse(1L, "Alfaguara")));
        when(genreService.findAll()).thenReturn(List.of(new CatalogResponse(1L, "Novela")));

        mockMvc.perform(get("/api/editorials").with(httpBasic("student", "student123")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/genres").with(httpBasic("student", "student123")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowCatalogWritesOnlyForAdmin() throws Exception {
        CatalogResponse response = new CatalogResponse(1L, "New", true);
        when(editorialService.findById(1L)).thenReturn(response);
        when(genreService.findById(1L)).thenReturn(response);
        when(editorialService.create(org.mockito.ArgumentMatchers.any())).thenReturn(response);
        when(editorialService.update(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any())).thenReturn(response);
        when(genreService.create(org.mockito.ArgumentMatchers.any())).thenReturn(response);
        when(genreService.update(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any())).thenReturn(response);

        mockMvc.perform(get("/api/editorials/1").with(httpBasic("student", "student123")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/genres/1").with(httpBasic("student", "student123")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/editorials").with(httpBasic("student", "student123"))
                        .contentType("application/json").content("{\"name\":\"New\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/editorials").with(httpBasic("admin", "admin123"))
                        .contentType("application/json").content("{\"name\":\"New\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/editorials/1")
                        .with(httpBasic("admin", "admin123")).contentType("application/json")
                        .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/editorials/1")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/genres").with(httpBasic("admin", "admin123"))
                        .contentType("application/json").content("{\"name\":\"New\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/genres/1")
                        .with(httpBasic("admin", "admin123")).contentType("application/json")
                        .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/genres/1")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());
    }
}
