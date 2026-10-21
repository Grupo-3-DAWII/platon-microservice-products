package edu.cibertec.products.controller;

import edu.cibertec.products.dto.CatalogRequest;
import edu.cibertec.products.dto.CatalogResponse;
import edu.cibertec.products.service.GenreService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
@RequestMapping({"/api/products/genres", "/api/v1/products/genres"})
@Tag(name = "Genres", description = "Genre catalog management")
public class GenreController {

    private final GenreService genreService;

    @GetMapping()
    public List<CatalogResponse> findAll() {
        return genreService.findAll();
    }

    @GetMapping("/{id}")
    public CatalogResponse findById(@PathVariable Long id) {
        return genreService.findById(id);
    }

    @PostMapping()
    public CatalogResponse create(@Valid @RequestBody CatalogRequest request) {
        return genreService.create(request);
    }

    @PutMapping("/{id}")
    public CatalogResponse update(@PathVariable Long id, @Valid @RequestBody CatalogRequest request) {
        return genreService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        genreService.delete(id);
    }
}