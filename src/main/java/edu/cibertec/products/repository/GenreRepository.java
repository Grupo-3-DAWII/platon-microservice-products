package edu.cibertec.products.repository;

import edu.cibertec.products.domain.entity.Genre;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface GenreRepository extends ListCrudRepository<Genre, Long> {
    List<Genre> findAllByActiveTrue();
    Optional<Genre> findByIdGenreAndActiveTrue(Long idGenre);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdGenreNot(String name, Long idGenre);
}
