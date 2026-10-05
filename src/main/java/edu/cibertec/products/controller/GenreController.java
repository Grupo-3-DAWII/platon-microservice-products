package edu.cibertec.products.controller;

import edu.cibertec.products.dto.CatalogRequest;
import edu.cibertec.products.dto.CatalogResponse;
import edu.cibertec.products.service.GenreService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
@Tag(name = "Genres", description = "Genre catalog management")
public class GenreController {

    private final GenreService genreService;

    @GetMapping("/api/genres")
    public List<CatalogResponse> findAll() {
        return genreService.findAll();
    }

    @GetMapping("/api/genres/{id}")
    public CatalogResponse findById(@PathVariable Long id) {
        return genreService.findById(id);
    }

    @PostMapping("/api/genres")
    public CatalogResponse create(@Valid @RequestBody CatalogRequest request) {
        return genreService.create(request);
    }

    @PutMapping("/api/genres/{id}")
    public CatalogResponse update(@PathVariable Long id, @Valid @RequestBody CatalogRequest request) {
        return genreService.update(id, request);
    }

    @DeleteMapping("/api/genres/{id}")
    public void delete(@PathVariable Long id) {
        genreService.delete(id);
    }
}
