package edu.cibertec.products.controller;

import edu.cibertec.products.dto.CatalogRequest;
import edu.cibertec.products.dto.CatalogResponse;
import edu.cibertec.products.service.EditorialService;
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
@Tag(name = "Editorials", description = "Editorial catalog management")
public class EditorialController {

    private final EditorialService editorialService;

    @GetMapping("/api/editorials")
    public List<CatalogResponse> findAll() {
        return editorialService.findAll();
    }

    @GetMapping("/api/editorials/{id}")
    public CatalogResponse findById(@PathVariable Long id) {
        return editorialService.findById(id);
    }

    @PostMapping("/api/editorials")
    public CatalogResponse create(@Valid @RequestBody CatalogRequest request) {
        return editorialService.create(request);
    }

    @PutMapping("/api/editorials/{id}")
    public CatalogResponse update(@PathVariable Long id, @Valid @RequestBody CatalogRequest request) {
        return editorialService.update(id, request);
    }

    @DeleteMapping("/api/editorials/{id}")
    public void delete(@PathVariable Long id) {
        editorialService.delete(id);
    }
}
