package edu.cibertec.products.service.impl;

import edu.cibertec.products.dto.CatalogResponse;
import edu.cibertec.products.mapper.CatalogMapper;
import edu.cibertec.products.repository.GenreRepository;
import edu.cibertec.products.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final CatalogMapper catalogMapper;

    @Override
    public List<CatalogResponse> findAll() {
        return genreRepository.findAll().stream().map(catalogMapper::toResponse).toList();
    }
}
