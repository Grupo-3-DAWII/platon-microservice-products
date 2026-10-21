package edu.cibertec.products.dto;

import java.util.UUID;

public record PurchaseAvailabilityResponse(
        UUID requestId, String eventType, Long idProducts, Integer numberSold, boolean available, String reason
) {}
