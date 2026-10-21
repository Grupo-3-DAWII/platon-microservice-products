package edu.cibertec.products.service;

import edu.cibertec.products.domain.entity.StockMovement;
import edu.cibertec.products.domain.entity.StockMovementType;
import edu.cibertec.products.dto.PaymentRejectedEvent;
import edu.cibertec.products.repository.ProductRepository;
import edu.cibertec.products.repository.StockDeductionRepository;
import edu.cibertec.products.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StockReturnService {
    private final StockDeductionRepository deductions;
    private final ProductRepository products;
    private final StockMovementRepository movements;

    @Transactional
    public void restore(PaymentRejectedEvent event) {
        deductions.lock(event.stockDeductionId());
        var existing = deductions.find(event.stockDeductionId());
        if (existing.isEmpty()) {
            // Impide que una solicitud atrasada descuente stock despues de su cancelacion.
            deductions.insert(event.stockDeductionId(), event.idProducts(), event.numberSold(), "RETURNED");
            return;
        }
        var deduction = existing.get();
        if (!Objects.equals(deduction.product(), event.idProducts())
                || !Objects.equals(deduction.quantity(), event.numberSold())) {
            throw new IllegalArgumentException("La devolucion no coincide con el descuento original");
        }
        if ("RETURNED".equals(deduction.status())) {
            return;
        }
        var product = products.findByIdForUpdate(deduction.product())
                .orElseThrow(() -> new IllegalStateException("No se encontro el producto del descuento"));
        LocalDateTime now = LocalDateTime.now();
        if (!products.incrementStock(deduction.product(), deduction.quantity(), now)) {
            throw new IllegalStateException("No se pudo devolver el stock");
        }
        movements.save(StockMovement.builder().idProduct(deduction.product()).type(StockMovementType.ENTRY)
                .quantity(deduction.quantity()).resultingStock(product.getStock() + deduction.quantity())
                .movementDate(now).createdAt(now).notes("Pago rechazado " + deduction.id()).build());
        deductions.markReturned(deduction.id());
    }
}
