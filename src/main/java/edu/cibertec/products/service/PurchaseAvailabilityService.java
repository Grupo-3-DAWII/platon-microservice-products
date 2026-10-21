package edu.cibertec.products.service;

import edu.cibertec.products.dto.PurchaseAvailabilityRequest;
import edu.cibertec.products.dto.PurchaseAvailabilityResponse;
import edu.cibertec.products.repository.ProductRepository;
import edu.cibertec.products.repository.StockDeductionRepository;
import edu.cibertec.products.repository.StockMovementRepository;
import edu.cibertec.products.domain.entity.StockMovement;
import edu.cibertec.products.domain.entity.StockMovementType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseAvailabilityService {

    private final ProductRepository productRepository;
    private final StockDeductionRepository deductions;
    private final StockMovementRepository movements;

    public PurchaseAvailabilityResponse check(PurchaseAvailabilityRequest request) {
        if (request.requestId() == null || !"purchase.requested".equals(request.eventType())
                || request.idProducts() == null || request.idProducts() <= 0
                || request.numberSold() == null || request.numberSold() <= 0 || request.expiresAt() == null) {
            return response(request, false, "INVALID_REQUEST");
        }
        if (!request.expiresAt().isAfter(Instant.now())) {
            return response(request, false, "REQUEST_EXPIRED");
        }
        deductions.lock(request.requestId());
        var existing = deductions.find(request.requestId());
        if (existing.isPresent()) {
            var deduction = existing.get();
            if (!Objects.equals(deduction.product(), request.idProducts())
                    || !Objects.equals(deduction.quantity(), request.numberSold())) {
                return response(request, false, "INVALID_REQUEST");
            }
            boolean deducted = "DEDUCTED".equals(deduction.status());
            return response(request, deducted, deducted ? "STOCK_DEDUCTED" : "STOCK_ALREADY_RETURNED");
        }
        var product = productRepository.findByIdForUpdate(request.idProducts());
        if (product.isEmpty()) {
            return response(request, false, "PRODUCT_NOT_FOUND");
        }
        if (!Boolean.TRUE.equals(product.get().getActive())) {
            return response(request, false, "PRODUCT_INACTIVE");
        }
        Integer stock = product.get().getStock();
        if (stock == null || stock < request.numberSold()) {
            return response(request, false, "INSUFFICIENT_STOCK");
        }
        LocalDateTime now = LocalDateTime.now();
        if (!productRepository.decrementStockIfAvailable(request.idProducts(), request.numberSold(), now)) {
            return response(request, false, "INSUFFICIENT_STOCK");
        }
        deductions.insert(request.requestId(), request.idProducts(), request.numberSold(), "DEDUCTED");
        movements.save(StockMovement.builder().idProduct(request.idProducts()).type(StockMovementType.EXIT)
                .quantity(request.numberSold()).movementDate(now).createdAt(now)
                .resultingStock(stock - request.numberSold()).notes("Solicitud de compra " + request.requestId()).build());
        return response(request, true, "STOCK_DEDUCTED");
    }

    private PurchaseAvailabilityResponse response(PurchaseAvailabilityRequest request, boolean available, String reason) {
        return new PurchaseAvailabilityResponse(request.requestId(), "purchase.availability.checked",
                request.idProducts(), request.numberSold(), available, reason);
    }
}
