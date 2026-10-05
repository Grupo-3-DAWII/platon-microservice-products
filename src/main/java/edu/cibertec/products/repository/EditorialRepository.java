package edu.cibertec.products.repository;

import edu.cibertec.products.domain.entity.Editorial;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface EditorialRepository extends ListCrudRepository<Editorial, Long> {
    List<Editorial> findAllByActiveTrue();
    Optional<Editorial> findByIdEditorialAndActiveTrue(Long idEditorial);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdEditorialNot(String name, Long idEditorial);
}
