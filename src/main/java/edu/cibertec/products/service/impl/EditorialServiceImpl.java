package edu.cibertec.products.service.impl;

import edu.cibertec.products.dto.CatalogResponse;
import edu.cibertec.products.dto.CatalogRequest;
import edu.cibertec.products.domain.entity.Editorial;
import edu.cibertec.products.exception.DuplicateResourceException;
import edu.cibertec.products.exception.ResourceNotFoundException;
import edu.cibertec.products.mapper.CatalogMapper;
import edu.cibertec.products.repository.EditorialRepository;
import edu.cibertec.products.service.EditorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EditorialServiceImpl implements EditorialService {

    private final EditorialRepository editorialRepository;
    private final CatalogMapper catalogMapper;

    @Override
    public List<CatalogResponse> findAll() {
        return editorialRepository.findAllByActiveTrue().stream().map(catalogMapper::toResponse).toList();
    }

    @Override
    public CatalogResponse findById(Long id) {
        return catalogMapper.toResponse(findEditorial(id));
    }

    @Override
    @Transactional
    public CatalogResponse create(CatalogRequest request) {
        String name = normalize(request);
        ensureUnique(name, null);
        return catalogMapper.toResponse(editorialRepository.save(catalogMapper.toEditorial(new CatalogRequest(name))));
    }

    @Override
    @Transactional
    public CatalogResponse update(Long id, CatalogRequest request) {
        Editorial editorial = findEditorial(id);
        String name = normalize(request);
        ensureUnique(name, id);
        catalogMapper.updateEditorial(new CatalogRequest(name), editorial);
        return catalogMapper.toResponse(editorialRepository.save(editorial));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Editorial editorial = findEditorial(id);
        editorial.setActive(false);
        editorialRepository.save(editorial);
    }

    private Editorial findEditorial(Long id) {
        return editorialRepository.findByIdEditorialAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Editorial not found"));
    }

    private String normalize(CatalogRequest request) {
        return request.name().trim();
    }

    private void ensureUnique(String name, Long id) {
        boolean exists = id == null ? editorialRepository.existsByNameIgnoreCase(name)
                : editorialRepository.existsByNameIgnoreCaseAndIdEditorialNot(name, id);
        if (exists) {
            throw new DuplicateResourceException("Editorial name already exists");
        }
    }
}
