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
        Product product = mapper.toEntity(request("Don Quijote", 5));
        mapper.updateEntity(product, request("Don Quijote Updated", 0));

        assertThat(product.getName()).isEqualTo("Don Quijote Updated");
        assertThat(product.getStock()).isZero();
    }

    @Test
    void shouldMapAvailableProduct() {
        ProductResponse response = mapper.toResponse(Product.builder().idProduct(1L)
                .name("Don Quijote").stock(5).build(), "Alfaguara", "Novela");

        assertThat(response.available()).isTrue();
    }

    @Test
    void shouldMapOutOfStockProduct() {
        ProductResponse response = mapper.toResponse(Product.builder().idProduct(1L)
                .name("Don Quijote").stock(0).build(), "Alfaguara", "Novela");

        assertThat(response.available()).isFalse();
    }

    @Test
    void shouldTreatNullStockAsUnavailable() {
        ProductResponse response = mapper.toResponse(Product.builder()
                .idProduct(1L).name("Don Quijote").stock(null).build(), "Alfaguara", "Novela");

        assertThat(response.available()).isFalse();
    }

    @Test
    void shouldHandleNullValues() {
        assertThat(mapper.toEntity(null)).isNull();
        Product product = Product.builder().idProduct(1L).build();
        mapper.updateEntity(product, null);
        assertThat(mapper.toResponse(null, null, null)).isNull();
        assertThat(mapper.toResponse(null, "Alfaguara", "Novela").available()).isFalse();
        assertThat(mapper.toResponse(null, null, "Novela").available()).isFalse();
        assertThat(mapper.toResponse(null, "Alfaguara", null).available()).isFalse();
    }

    @Test
    void shouldCopyImageValues() {
        ProductRequest request = new ProductRequest("Don Quijote", "978-612-00-0001-1", "Miguel de Cervantes",
                1L, 1L, 1605, "Las aventuras del ingenioso hidalgo.", new byte[]{1, 2}, "image/png",
                new java.math.BigDecimal("80.00"), new java.math.BigDecimal("60.00"), true, 5);

        Product product = mapper.toEntity(request);
        mapper.updateEntity(product, request);
        ProductResponse response = mapper.toResponse(product, "Alfaguara", "Novela");

        assertThat(product.getImage()).containsExactly(1, 2);
        assertThat(response.image()).containsExactly(1, 2);
    }

    private ProductRequest request(String name, Integer stock) {
        return new ProductRequest(name, "978-612-00-0001-1", "Miguel de Cervantes",
                1L, 1L, 1605, "Las aventuras del ingenioso hidalgo.", null, null,
                new java.math.BigDecimal("80.00"), new java.math.BigDecimal("60.00"), true, stock);
    }
}
