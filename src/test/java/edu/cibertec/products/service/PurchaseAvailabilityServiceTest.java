package edu.cibertec.products.service;

import edu.cibertec.products.domain.entity.Product;
import edu.cibertec.products.dto.PurchaseAvailabilityRequest;
import edu.cibertec.products.repository.ProductRepository;
import edu.cibertec.products.repository.StockDeductionRepository;
import edu.cibertec.products.repository.StockMovementRepository;
import edu.cibertec.products.domain.entity.StockMovement;
import edu.cibertec.products.domain.entity.StockMovementType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseAvailabilityServiceTest {
    @Mock private ProductRepository repository;
    @Mock private StockDeductionRepository deductions;
    @Mock private StockMovementRepository movements;
    @InjectMocks private PurchaseAvailabilityService service;

    @Test
    void deductsStockAndRecordsExitBeforeApproving() {
        var product = Product.builder().idProduct(1L).active(true).stock(2).build();
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(repository.decrementStockIfAvailable(eq(1L), eq(2), any())).thenReturn(true);
        var request = request(2);
        var response = service.check(request);
        assertThat(response.available()).isTrue();
        assertThat(response.reason()).isEqualTo("STOCK_DEDUCTED");
        assertThat(response.requestId()).isEqualTo(request.requestId());
        assertThat(response.eventType()).isEqualTo("purchase.availability.checked");
        verify(deductions).insert(request.requestId(), 1L, 2, "DEDUCTED");
        verify(movements).save(argThat(movement -> movement.getType() == StockMovementType.EXIT
                && movement.getQuantity() == 2 && movement.getResultingStock() == 0));
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsMissingProduct() {
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.empty());
        assertThat(service.check(request(1)).reason()).isEqualTo("PRODUCT_NOT_FOUND");
    }

    @Test
    void rejectsInactiveProduct() {
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(Product.builder().active(false).stock(5).build()));
        var response = service.check(request(1));
        assertThat(response.available()).isFalse();
        assertThat(response.reason()).isEqualTo("PRODUCT_INACTIVE");
    }

    @Test
    void rejectsInsufficientOrUnknownStock() {
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(Product.builder().active(true).stock(1).build()));
        assertThat(service.check(request(2)).reason()).isEqualTo("INSUFFICIENT_STOCK");
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(Product.builder().active(true).build()));
        assertThat(service.check(request(1)).reason()).isEqualTo("INSUFFICIENT_STOCK");
    }

    @Test
    void doesNotDeductAgainAndRejectsCanceledOrMismatchedDeduction() {
        var request = request(1);
        when(deductions.find(request.requestId())).thenReturn(Optional.of(
                new StockDeductionRepository.Deduction(request.requestId(), 1L, 1, "DEDUCTED")));
        assertThat(service.check(request).available()).isTrue();
        verifyNoInteractions(repository, movements);
        when(deductions.find(request.requestId())).thenReturn(Optional.of(
                new StockDeductionRepository.Deduction(request.requestId(), 1L, 1, "RETURNED")));
        assertThat(service.check(request).available()).isFalse();
        when(deductions.find(request.requestId())).thenReturn(Optional.of(
                new StockDeductionRepository.Deduction(request.requestId(), 2L, 1, "DEDUCTED")));
        assertThat(service.check(request).reason()).isEqualTo("INVALID_REQUEST");
        when(deductions.find(request.requestId())).thenReturn(Optional.of(
                new StockDeductionRepository.Deduction(request.requestId(), 1L, 2, "DEDUCTED")));
        assertThat(service.check(request).reason()).isEqualTo("INVALID_REQUEST");
    }

    @Test
    void rejectsWhenAtomicStockUpdateFails() {
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(Product.builder().active(true).stock(1).build()));
        assertThat(service.check(request(1)).reason()).isEqualTo("INSUFFICIENT_STOCK");
        verifyNoInteractions(movements);
    }

    @Test
    void rejectsExpiredRequestWithoutQueryingDatabase() {
        var request = new PurchaseAvailabilityRequest(UUID.randomUUID(), "purchase.requested", 1L, 1,
                Instant.now().minusSeconds(1));
        assertThat(service.check(request).reason()).isEqualTo("REQUEST_EXPIRED");
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void rejectsInvalidRequestsWithoutQueryingDatabase(PurchaseAvailabilityRequest request) {
        assertThat(service.check(request).reason()).isEqualTo("INVALID_REQUEST");
        verifyNoInteractions(repository);
    }

    static Stream<PurchaseAvailabilityRequest> invalidRequests() {
        UUID id = UUID.randomUUID();
        Instant future = Instant.now().plusSeconds(60);
        return Stream.of(
                new PurchaseAvailabilityRequest(null, "purchase.requested", 1L, 1, future),
                new PurchaseAvailabilityRequest(id, "other.event", 1L, 1, future),
                new PurchaseAvailabilityRequest(id, "purchase.requested", null, 1, future),
                new PurchaseAvailabilityRequest(id, "purchase.requested", 0L, 1, future),
                new PurchaseAvailabilityRequest(id, "purchase.requested", 1L, null, future),
                new PurchaseAvailabilityRequest(id, "purchase.requested", 1L, 0, future),
                new PurchaseAvailabilityRequest(id, "purchase.requested", 1L, 1, null));
    }

    private PurchaseAvailabilityRequest request(int quantity) {
        return new PurchaseAvailabilityRequest(UUID.randomUUID(), "purchase.requested", 1L, quantity,
                Instant.now().plusSeconds(60));
    }
}
