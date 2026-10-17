package edu.cibertec.products.mapper;

import edu.cibertec.products.domain.entity.Product;
import edu.cibertec.products.dto.ProductRequest;
import edu.cibertec.products.dto.ProductResponse;
import edu.cibertec.products.dto.ProductSummaryResponse;
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
    @Mapping(target = "imageUrl", source = "image")
    @Mapping(target = "stock", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(target = "idProduct", ignore = true)
    @Mapping(target = "idEditorial", ignore = true)
    @Mapping(target = "idGenre", ignore = true)
    @Mapping(target = "salePrice", ignore = true)
    @Mapping(target = "imageUrl", source = "image")
    @Mapping(target = "stock", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Product product, ProductRequest request);

    @Mapping(target = "editorial", source = "editorialName")
    @Mapping(target = "genre", source = "genreName")
    @Mapping(target = "image", source = "product.imageUrl")
    @Mapping(target = "available", expression = "java(Objects.nonNull(product) && Objects.nonNull(product.getStock()) && product.getStock() > 0)")
    ProductSummaryResponse toSummary(Product product, String editorialName, String genreName);

    @Mapping(target = "editorial", source = "editorialName")
    @Mapping(target = "editorialId", source = "product.idEditorial")
    @Mapping(target = "genre", source = "genreName")
    @Mapping(target = "genreId", source = "product.idGenre")
    @Mapping(target = "image", source = "product.imageUrl")
    @Mapping(target = "available", expression = "java(Objects.nonNull(product) && Objects.nonNull(product.getStock()) && product.getStock() > 0)")
    @Mapping(target = "lastMovement", ignore = true)
    ProductResponse toResponse(Product product, String editorialName, String genreName);
}
