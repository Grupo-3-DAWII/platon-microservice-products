package edu.cibertec.products.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cibertec.products.dto.PaymentRejectedEvent;
import edu.cibertec.products.service.StockReturnService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.MessageBuilder;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentRejectedListenerTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final StockReturnService service = mock(StockReturnService.class);
    private final PaymentRejectedListener listener = new PaymentRejectedListener(mapper, service);

    @Test
    void passesValidEventToStockService() throws Exception {
        var event = new PaymentRejectedEvent(UUID.randomUUID(), "payment.rejected", UUID.randomUUID(), 1L, 2);
        listener.receive(MessageBuilder.withBody(mapper.writeValueAsBytes(event)).build());
        verify(service).restore(event);
    }

    @Test
    void rejectsMalformedEventsWithoutRequeueing() {
        for (String json : new String[]{"invalid", "null", "{}",
                "{\"eventId\":\"" + UUID.randomUUID() + "\",\"eventType\":\"wrong\"}"}) {
            assertThatThrownBy(() -> listener.receive(MessageBuilder.withBody(json.getBytes(StandardCharsets.UTF_8)).build()))
                    .isInstanceOf(AmqpRejectAndDontRequeueException.class);
        }
        verifyNoInteractions(service);
    }

    @Test
    void preservesTransientFailureForRequeue() throws Exception {
        var event = new PaymentRejectedEvent(UUID.randomUUID(), "payment.rejected", UUID.randomUUID(), 1L, 2);
        doThrow(new IllegalStateException("database offline")).when(service).restore(event);
        assertThatThrownBy(() -> listener.receive(MessageBuilder.withBody(mapper.writeValueAsBytes(event)).build()))
                .isInstanceOf(IllegalStateException.class);
    }
}
