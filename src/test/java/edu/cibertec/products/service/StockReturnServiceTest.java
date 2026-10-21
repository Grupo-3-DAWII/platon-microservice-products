package edu.cibertec.products.service;

import edu.cibertec.products.domain.entity.Product;
import edu.cibertec.products.domain.entity.StockMovementType;
import edu.cibertec.products.dto.PaymentRejectedEvent;
import edu.cibertec.products.repository.ProductRepository;
import edu.cibertec.products.repository.StockDeductionRepository;
import edu.cibertec.products.repository.StockMovementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockReturnServiceTest {
    @Mock private StockDeductionRepository deductions;
    @Mock private ProductRepository products;
    @Mock private StockMovementRepository movements;
    @InjectMocks private StockReturnService service;
    private final UUID id = UUID.randomUUID();
    private final PaymentRejectedEvent event = new PaymentRejectedEvent(UUID.randomUUID(), "payment.rejected", id, 1L, 2);

    @Test
    void restoresOriginalQuantityAndMarksItReturned() {
        when(deductions.find(id)).thenReturn(Optional.of(new StockDeductionRepository.Deduction(id, 1L, 2, "DEDUCTED")));
        when(products.findByIdForUpdate(1L)).thenReturn(Optional.of(Product.builder().stock(3).build()));
        when(products.incrementStock(eq(1L), eq(2), any())).thenReturn(true);
        service.restore(event);
        verify(deductions).markReturned(id);
        verify(movements).save(argThat(movement -> movement.getQuantity() == 2
                && movement.getResultingStock() == 5 && movement.getType() == StockMovementType.ENTRY));
    }

    @Test
    void repeatedEventsDoNotAddStockAgain() {
        when(deductions.find(id)).thenReturn(Optional.of(new StockDeductionRepository.Deduction(id, 1L, 2, "RETURNED")));
        service.restore(event);
        verifyNoInteractions(products, movements);
    }

    @Test
    void cancellationBeforeDeductionPreventsLateDeductionWithoutAddingStock() {
        service.restore(event);
        verify(deductions).insert(id, 1L, 2, "RETURNED");
        verifyNoInteractions(products, movements);
    }

    @Test
    void rejectsMismatchedProductOrQuantity() {
        when(deductions.find(id)).thenReturn(Optional.of(new StockDeductionRepository.Deduction(id, 2L, 2, "DEDUCTED")));
        assertThatThrownBy(() -> service.restore(event)).isInstanceOf(IllegalArgumentException.class);
        when(deductions.find(id)).thenReturn(Optional.of(new StockDeductionRepository.Deduction(id, 1L, 1, "DEDUCTED")));
        assertThatThrownBy(() -> service.restore(event)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(products, movements);
    }

    @Test
    void transientFailuresRemainErrorsSoConsumerCanRetry() {
        when(deductions.find(id)).thenReturn(Optional.of(new StockDeductionRepository.Deduction(id, 1L, 2, "DEDUCTED")));
        assertThatThrownBy(() -> service.restore(event)).isInstanceOf(IllegalStateException.class);
        when(products.findByIdForUpdate(1L)).thenReturn(Optional.of(Product.builder().stock(3).build()));
        assertThatThrownBy(() -> service.restore(event)).isInstanceOf(IllegalStateException.class);
        verify(deductions, never()).markReturned(any());
    }
}
