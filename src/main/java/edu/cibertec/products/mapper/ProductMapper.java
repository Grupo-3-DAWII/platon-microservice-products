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

    @Mapping(target = "idProduct", ignore = true)
    @Mapping(target = "idEditorial", ignore = true)
    @Mapping(target = "idGenre", ignore = true)
    @Mapping(target = "salePrice", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(target = "idProduct", ignore = true)
    @Mapping(target = "idEditorial", ignore = true)
    @Mapping(target = "idGenre", ignore = true)
    @Mapping(target = "salePrice", ignore = true)
    void updateEntity(@MappingTarget Product product, ProductRequest request);

    @Mapping(target = "editorial", source = "editorialName")
    @Mapping(target = "editorialId", source = "product.idEditorial")
    @Mapping(target = "genre", source = "genreName")
    @Mapping(target = "genreId", source = "product.idGenre")
    @Mapping(target = "available", expression = "java(Objects.nonNull(product) && Objects.nonNull(product.getStock()) && product.getStock() > 0)")
    ProductResponse toResponse(Product product, String editorialName, String genreName);
}
