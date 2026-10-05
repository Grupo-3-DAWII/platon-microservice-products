package edu.cibertec.products.service;

import edu.cibertec.products.domain.entity.Editorial;
import edu.cibertec.products.domain.entity.Genre;
import edu.cibertec.products.dto.CatalogResponse;
import edu.cibertec.products.dto.CatalogRequest;
import edu.cibertec.products.exception.DuplicateResourceException;
import edu.cibertec.products.exception.ResourceNotFoundException;
import edu.cibertec.products.mapper.CatalogMapper;
import edu.cibertec.products.repository.EditorialRepository;
import edu.cibertec.products.repository.GenreRepository;
import edu.cibertec.products.service.impl.EditorialServiceImpl;
import edu.cibertec.products.service.impl.GenreServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private EditorialRepository editorialRepository;

    @Mock
    private GenreRepository genreRepository;

    @Spy
    private CatalogMapper catalogMapper = Mappers.getMapper(CatalogMapper.class);

    @InjectMocks
    private EditorialServiceImpl editorialService;

    @InjectMocks
    private GenreServiceImpl genreService;

    @Test
    void shouldFindEditorials() {
        when(editorialRepository.findAllByActiveTrue()).thenReturn(List.of(Editorial.builder()
                .idEditorial(1L).name("Alfaguara").active(true).build()));

        assertThat(editorialService.findAll()).containsExactly(new CatalogResponse(1L, "Alfaguara"));
    }

    @Test
    void shouldFindGenres() {
        when(genreRepository.findAllByActiveTrue()).thenReturn(List.of(Genre.builder()
                .idGenre(1L).name("Novela").active(true).build()));

        assertThat(genreService.findAll()).containsExactly(new CatalogResponse(1L, "Novela"));
    }

    @Test
    void shouldHandleNullCatalogEntities() {
        assertThat(catalogMapper.toResponse((Editorial) null)).isNull();
        assertThat(catalogMapper.toResponse((Genre) null)).isNull();
        assertThat(catalogMapper.toEditorial(null)).isNull();
        assertThat(catalogMapper.toGenre(null)).isNull();
        catalogMapper.updateEditorial(null, null);
        catalogMapper.updateGenre(null, null);
    }

    @Test
    void shouldCreateUpdateAndDeleteEditorial() {
        Editorial created = Editorial.builder().idEditorial(2L).name("Planeta").active(true).build();
        when(editorialRepository.existsByNameIgnoreCase("Planeta")).thenReturn(false);
        when(editorialRepository.save(any(Editorial.class))).thenReturn(created);
        when(editorialRepository.findByIdEditorialAndActiveTrue(2L)).thenReturn(java.util.Optional.of(created));

        assertThat(editorialService.create(new CatalogRequest(" Planeta ")))
                .isEqualTo(new CatalogResponse(2L, "Planeta", true));
        assertThat(editorialService.update(2L, new CatalogRequest("Updated")))
                .isEqualTo(new CatalogResponse(2L, "Updated", true));
        editorialService.delete(2L);
        assertThat(created.isActive()).isFalse();
    }

    @Test
    void shouldRejectDuplicateEditorialAndMissingEditorial() {
        when(editorialRepository.existsByNameIgnoreCase("Alfaguara")).thenReturn(true);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> editorialService.create(new CatalogRequest("Alfaguara")))
                .isInstanceOf(DuplicateResourceException.class);
        when(editorialRepository.findByIdEditorialAndActiveTrue(9L)).thenReturn(java.util.Optional.empty());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> editorialService.findById(9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldFindEditorialById() {
        Editorial editorial = Editorial.builder().idEditorial(1L).name("Alfaguara").active(true).build();
        when(editorialRepository.findByIdEditorialAndActiveTrue(1L)).thenReturn(java.util.Optional.of(editorial));
        assertThat(editorialService.findById(1L)).isEqualTo(new CatalogResponse(1L, "Alfaguara", true));
    }

    @Test
    void shouldCreateUpdateAndDeleteGenre() {
        Genre created = Genre.builder().idGenre(2L).name("Ensayo").active(true).build();
        when(genreRepository.existsByNameIgnoreCase("Ensayo")).thenReturn(false);
        when(genreRepository.save(any(Genre.class))).thenReturn(created);
        when(genreRepository.findByIdGenreAndActiveTrue(2L)).thenReturn(java.util.Optional.of(created));

        assertThat(genreService.create(new CatalogRequest(" Ensayo ")))
                .isEqualTo(new CatalogResponse(2L, "Ensayo", true));
        genreService.update(2L, new CatalogRequest("Updated"));
        genreService.delete(2L);
        assertThat(created.isActive()).isFalse();
    }

    @Test
    void shouldRejectDuplicateGenreAndMissingGenre() {
        when(genreRepository.existsByNameIgnoreCase("Novela")).thenReturn(true);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> genreService.create(new CatalogRequest("Novela")))
                .isInstanceOf(DuplicateResourceException.class);
        when(genreRepository.findByIdGenreAndActiveTrue(9L)).thenReturn(java.util.Optional.empty());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> genreService.findById(9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldFindGenreById() {
        Genre genre = Genre.builder().idGenre(1L).name("Novela").active(true).build();
        when(genreRepository.findByIdGenreAndActiveTrue(1L)).thenReturn(java.util.Optional.of(genre));
        assertThat(genreService.findById(1L)).isEqualTo(new CatalogResponse(1L, "Novela", true));
    }
}
