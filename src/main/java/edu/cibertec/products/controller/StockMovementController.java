package edu.cibertec.products.controller;

import edu.cibertec.products.dto.StockMovementRequest;
import edu.cibertec.products.dto.StockMovementResponse;
import edu.cibertec.products.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products/{productId}/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @PostMapping
    public ResponseEntity<StockMovementResponse> create(@PathVariable Long productId,
                                                        @Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockMovementService.create(productId, request));
    }

    @GetMapping
    public List<StockMovementResponse> findByProduct(@PathVariable Long productId) {
        return stockMovementService.findByProduct(productId);
    }
}
