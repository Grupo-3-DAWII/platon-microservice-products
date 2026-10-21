package edu.cibertec.products.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentRejectedEvent(
        UUID eventId, String eventType, UUID stockDeductionId, Long idProducts, Integer numberSold
) {}
