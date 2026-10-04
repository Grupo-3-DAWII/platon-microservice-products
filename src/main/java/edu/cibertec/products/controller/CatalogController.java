package edu.cibertec.products.controller;

import edu.cibertec.products.dto.CatalogResponse;
import edu.cibertec.products.service.EditorialService;
import edu.cibertec.products.service.GenreService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
@Tag(name = "Catalogs", description = "Editorial and genre catalogs")
public class CatalogController {

    private final EditorialService editorialService;
    private final GenreService genreService;

    @GetMapping("/api/editorials")
    public List<CatalogResponse> findEditorials() {
        return editorialService.findAll();
    }

    @GetMapping("/api/genres")
    public List<CatalogResponse> findGenres() {
        return genreService.findAll();
    }
}
