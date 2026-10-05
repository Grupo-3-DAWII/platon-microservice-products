package edu.cibertec.products.service.impl;

import edu.cibertec.products.dto.CatalogResponse;
import edu.cibertec.products.dto.CatalogRequest;
import edu.cibertec.products.domain.entity.Genre;
import edu.cibertec.products.exception.DuplicateResourceException;
import edu.cibertec.products.exception.ResourceNotFoundException;
import edu.cibertec.products.mapper.CatalogMapper;
import edu.cibertec.products.repository.GenreRepository;
import edu.cibertec.products.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final CatalogMapper catalogMapper;

    @Override
    public List<CatalogResponse> findAll() {
        return genreRepository.findAllByActiveTrue().stream().map(catalogMapper::toResponse).toList();
    }

    @Override
    public CatalogResponse findById(Long id) {
        return catalogMapper.toResponse(findGenre(id));
    }

    @Override
    @Transactional
    public CatalogResponse create(CatalogRequest request) {
        String name = normalize(request);
        ensureUnique(name, null);
        return catalogMapper.toResponse(genreRepository.save(catalogMapper.toGenre(new CatalogRequest(name))));
    }

    @Override
    @Transactional
    public CatalogResponse update(Long id, CatalogRequest request) {
        Genre genre = findGenre(id);
        String name = normalize(request);
        ensureUnique(name, id);
        catalogMapper.updateGenre(new CatalogRequest(name), genre);
        return catalogMapper.toResponse(genreRepository.save(genre));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Genre genre = findGenre(id);
        genre.setActive(false);
        genreRepository.save(genre);
    }

    private Genre findGenre(Long id) {
        return genreRepository.findByIdGenreAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found"));
    }

    private String normalize(CatalogRequest request) {
        return request.name().trim();
    }

    private void ensureUnique(String name, Long id) {
        boolean exists = id == null ? genreRepository.existsByNameIgnoreCase(name)
                : genreRepository.existsByNameIgnoreCaseAndIdGenreNot(name, id);
        if (exists) {
            throw new DuplicateResourceException("Genre name already exists");
        }
    }
}
