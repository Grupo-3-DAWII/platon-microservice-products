package edu.cibertec.products.service.impl;

import edu.cibertec.products.dto.CatalogResponse;
import edu.cibertec.products.mapper.CatalogMapper;
import edu.cibertec.products.repository.EditorialRepository;
import edu.cibertec.products.service.EditorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EditorialServiceImpl implements EditorialService {

    private final EditorialRepository editorialRepository;
    private final CatalogMapper catalogMapper;

    @Override
    public List<CatalogResponse> findAll() {
        return editorialRepository.findAll().stream().map(catalogMapper::toResponse).toList();
    }
}
