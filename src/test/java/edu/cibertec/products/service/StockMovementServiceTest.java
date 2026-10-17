package edu.cibertec.products.service;

import edu.cibertec.products.domain.entity.Product;
import edu.cibertec.products.domain.entity.StockMovement;
import edu.cibertec.products.domain.entity.StockMovementType;
import edu.cibertec.products.dto.StockMovementRequest;
import edu.cibertec.products.dto.StockMovementResponse;
import edu.cibertec.products.exception.InsufficientStockException;
import edu.cibertec.products.repository.ProductRepository;
import edu.cibertec.products.repository.StockMovementRepository;
import edu.cibertec.products.service.impl.StockMovementServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private StockMovementServiceImpl stockMovementService;

    @Test
    void shouldIncrementStockAtomicallyAndRecordMovement() {
        Product product = product(5);
        Product updated = product(8);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(productRepository.findById(1L)).thenReturn(Optional.of(updated));
        when(productRepository.incrementStock(eq(1L), eq(3), any())).thenReturn(true);
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockMovementResponse response = stockMovementService.create(1L,
                new StockMovementRequest(StockMovementType.ENTRY, 3, null, "Restock"));

        assertThat(response.resultingStock()).isEqualTo(8);
        verify(productRepository).incrementStock(eq(1L), eq(3), any());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldDecrementStockAtomically() {
        Product product = product(5);
        Product updated = product(2);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(productRepository.findById(1L)).thenReturn(Optional.of(updated));
        when(productRepository.decrementStockIfAvailable(eq(1L), eq(3), any())).thenReturn(true);
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockMovementResponse response = stockMovementService.create(1L,
                new StockMovementRequest(StockMovementType.EXIT, 3, null, null));

        assertThat(response.resultingStock()).isEqualTo(2);
        verify(productRepository).decrementStockIfAvailable(eq(1L), eq(3), any());
    }

    @Test
    void shouldRejectExitWhenAtomicUpdateFindsInsufficientStock() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product(2)));
        when(productRepository.decrementStockIfAvailable(eq(1L), eq(3), any())).thenReturn(false);

        assertThatThrownBy(() -> stockMovementService.create(1L,
                new StockMovementRequest(StockMovementType.EXIT, 3, null, null)))
                .isInstanceOf(InsufficientStockException.class);
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    void shouldListMovementsOnlyForExistingProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);
        when(stockMovementRepository.findByIdProductOrderByMovementDateDescIdMovementDesc(1L))
                .thenReturn(List.of());

        assertThat(stockMovementService.findByProduct(1L)).isEmpty();
    }

    private Product product(int stock) {
        return Product.builder().idProduct(1L).stock(stock).build();
    }
}
