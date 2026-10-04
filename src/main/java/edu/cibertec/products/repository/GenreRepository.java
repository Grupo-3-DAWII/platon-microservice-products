package edu.cibertec.products.repository;

import edu.cibertec.products.domain.entity.Genre;
import org.springframework.data.repository.ListCrudRepository;

public interface GenreRepository extends ListCrudRepository<Genre, Long> {
}
