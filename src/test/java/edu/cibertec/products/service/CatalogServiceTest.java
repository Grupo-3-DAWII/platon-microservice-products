package edu.cibertec.products.service;

import edu.cibertec.products.domain.entity.Editorial;
import edu.cibertec.products.domain.entity.Genre;
import edu.cibertec.products.dto.CatalogResponse;
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
import static org.mockito.Mockito.when;

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
        when(editorialRepository.findAll()).thenReturn(List.of(Editorial.builder()
                .idEditorial(1L).name("Alfaguara").build()));

        assertThat(editorialService.findAll()).containsExactly(new CatalogResponse(1L, "Alfaguara"));
    }

    @Test
    void shouldFindGenres() {
        when(genreRepository.findAll()).thenReturn(List.of(Genre.builder()
                .idGenre(1L).name("Novela").build()));

        assertThat(genreService.findAll()).containsExactly(new CatalogResponse(1L, "Novela"));
    }

    @Test
    void shouldHandleNullCatalogEntities() {
        assertThat(catalogMapper.toResponse((Editorial) null)).isNull();
        assertThat(catalogMapper.toResponse((Genre) null)).isNull();
    }
}
