package edu.cibertec.products.repository;

import edu.cibertec.products.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends ListCrudRepository<Product, Long>, PagingAndSortingRepository<Product, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdProductNot(String name, Long idProduct);

    boolean existsByIsbnIgnoreCase(String isbn);

    boolean existsByIsbnIgnoreCaseAndIdProductNot(String isbn, Long idProduct);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT * FROM product WHERE idproduct = :productId FOR UPDATE")
    Optional<Product> findByIdForUpdate(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE product SET stock = stock + :quantity, updated_at = :updatedAt WHERE idproduct = :productId")
    boolean incrementStock(@Param("productId") Long productId,
                           @Param("quantity") Integer quantity,
                           @Param("updatedAt") java.time.LocalDateTime updatedAt);

    @Modifying
    @Query("UPDATE product SET stock = stock - :quantity, updated_at = :updatedAt "
            + "WHERE idproduct = :productId AND stock >= :quantity")
    boolean decrementStockIfAvailable(@Param("productId") Long productId,
                                      @Param("quantity") Integer quantity,
                                      @Param("updatedAt") java.time.LocalDateTime updatedAt);
}
