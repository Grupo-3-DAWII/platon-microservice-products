package edu.cibertec.products.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cibertec.products.dto.PurchaseAvailabilityRequest;
import edu.cibertec.products.dto.PurchaseAvailabilityResponse;
import edu.cibertec.products.service.PurchaseAvailabilityService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PurchaseAvailabilityListenerTest {
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private final PurchaseAvailabilityService service = mock(PurchaseAvailabilityService.class);
    private final PurchaseAvailabilityListener listener = new PurchaseAvailabilityListener(mapper, service);

    @Test
    void returnsJsonEventForRabbitListenerToPublishAsReply() throws Exception {
        var request = new PurchaseAvailabilityRequest(UUID.randomUUID(), "purchase.requested", 1L, 2,
                Instant.now().plusSeconds(60));
        var expected = new PurchaseAvailabilityResponse(request.requestId(), "purchase.availability.checked",
                1L, 2, true, "AVAILABLE");
        when(service.check(request)).thenReturn(expected);
        Message response = listener.validate(MessageBuilder.withBody(mapper.writeValueAsBytes(request)).build());
        assertThat(mapper.readValue(response.getBody(), PurchaseAvailabilityResponse.class)).isEqualTo(expected);
        assertThat(response.getMessageProperties().getType()).isEqualTo("purchase.availability.checked");
        assertThat(response.getMessageProperties().getContentType()).isEqualTo("application/json");
        assertThat(response.getMessageProperties().getDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);
    }

    @Test
    void rejectsMalformedAndNullEventsWithoutRequeue() {
        assertThatThrownBy(() -> listener.validate(message("invalid json")))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
        assertThatThrownBy(() -> listener.validate(message("null")))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
        verifyNoInteractions(service);
    }

    private Message message(String body) {
        return MessageBuilder.withBody(body.getBytes(StandardCharsets.UTF_8)).build();
    }
}
