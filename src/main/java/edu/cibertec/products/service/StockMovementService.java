package edu.cibertec.products.service;

import edu.cibertec.products.dto.StockMovementRequest;
import edu.cibertec.products.dto.StockMovementResponse;

import java.util.List;

public interface StockMovementService {
    StockMovementResponse create(Long productId, StockMovementRequest request);
    List<StockMovementResponse> findByProduct(Long productId);
}
