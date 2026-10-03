package edu.cibertec.products.mapper;

import edu.cibertec.products.domain.entity.Product;
import edu.cibertec.products.dto.ProductRequest;
import edu.cibertec.products.dto.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Objects;

@Mapper(componentModel = "spring", imports = Objects.class)
public interface ProductMapper {

    Product toEntity(ProductRequest request);

    void updateEntity(@MappingTarget Product product, ProductRequest request);

    @Mapping(target = "available", expression = "java(Objects.nonNull(product.getStock()) && product.getStock() > 0)")
    ProductResponse toResponse(Product product);
}
