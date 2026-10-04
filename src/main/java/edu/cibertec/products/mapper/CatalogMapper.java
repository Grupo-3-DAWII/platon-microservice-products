package edu.cibertec.products.mapper;

import edu.cibertec.products.domain.entity.Editorial;
import edu.cibertec.products.domain.entity.Genre;
import edu.cibertec.products.dto.CatalogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CatalogMapper {

    @Mapping(target = "id", source = "idEditorial")
    CatalogResponse toResponse(Editorial editorial);

    @Mapping(target = "id", source = "idGenre")
    CatalogResponse toResponse(Genre genre);
}
