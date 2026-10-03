package edu.cibertec.products.mapper;

import edu.cibertec.products.domain.entity.Product;
import edu.cibertec.products.dto.ProductRequest;
import edu.cibertec.products.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void shouldMapRequestAndUpdateEntity() {
        Product product = mapper.toEntity(new ProductRequest("Don Quijote", 5));
        mapper.updateEntity(product, new ProductRequest("Don Quijote Updated", 0));

        assertThat(product.getName()).isEqualTo("Don Quijote Updated");
        assertThat(product.getStock()).isZero();
    }

    @Test
    void shouldMapAvailableProduct() {
        ProductResponse response = mapper.toResponse(Product.builder().idProduct(1L)
                .name("Don Quijote").stock(5).build());

        assertThat(response.available()).isTrue();
    }

    @Test
    void shouldMapOutOfStockProduct() {
        ProductResponse response = mapper.toResponse(Product.builder().idProduct(1L)
                .name("Don Quijote").stock(0).build());

        assertThat(response.available()).isFalse();
    }

    @Test
    void shouldTreatNullStockAsUnavailable() {
        ProductResponse response = mapper.toResponse(Product.builder()
                .idProduct(1L).name("Don Quijote").stock(null).build());

        assertThat(response.available()).isFalse();
    }
}
