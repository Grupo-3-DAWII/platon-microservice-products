package edu.cibertec.products.mapper;

import edu.cibertec.products.domain.entity.Editorial;
import edu.cibertec.products.domain.entity.Genre;
import edu.cibertec.products.dto.CatalogRequest;
import edu.cibertec.products.dto.CatalogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CatalogMapper {

    @Mapping(target = "id", source = "idEditorial")
    CatalogResponse toResponse(Editorial editorial);

    @Mapping(target = "id", source = "idGenre")
    CatalogResponse toResponse(Genre genre);

    @Mapping(target = "idEditorial", ignore = true)
    @Mapping(target = "active", constant = "true")
    Editorial toEditorial(CatalogRequest request);

    @Mapping(target = "idGenre", ignore = true)
    @Mapping(target = "active", constant = "true")
    Genre toGenre(CatalogRequest request);

    @Mapping(target = "idEditorial", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEditorial(CatalogRequest request, @MappingTarget Editorial editorial);

    @Mapping(target = "idGenre", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateGenre(CatalogRequest request, @MappingTarget Genre genre);
}
