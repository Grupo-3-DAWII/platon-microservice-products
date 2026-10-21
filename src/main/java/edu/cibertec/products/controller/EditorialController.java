package edu.cibertec.products.controller;

import edu.cibertec.products.dto.CatalogRequest;
import edu.cibertec.products.dto.CatalogResponse;
import edu.cibertec.products.service.EditorialService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
@RequestMapping({"/api/products/editorials", "/api/v1/products/editorials"})
@Tag(name = "Editorials", description = "Editorial catalog management")
public class EditorialController {

    private final EditorialService editorialService;

    @GetMapping()
    public List<CatalogResponse> findAll() {
        return editorialService.findAll();
    }

    @GetMapping("/{id}")
    public CatalogResponse findById(@PathVariable Long id) {
        return editorialService.findById(id);
    }

    @PostMapping()
    public CatalogResponse create(@Valid @RequestBody CatalogRequest request) {
        return editorialService.create(request);
    }

    @PutMapping("/{id}")
    public CatalogResponse update(@PathVariable Long id, @Valid @RequestBody CatalogRequest request) {
        return editorialService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        editorialService.delete(id);
    }
}