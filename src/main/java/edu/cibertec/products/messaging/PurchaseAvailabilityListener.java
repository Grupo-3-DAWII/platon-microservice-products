package edu.cibertec.products.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cibertec.products.dto.PurchaseAvailabilityRequest;
import edu.cibertec.products.service.PurchaseAvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PurchaseAvailabilityListener {

    private final ObjectMapper objectMapper;
    private final PurchaseAvailabilityService availabilityService;

    @RabbitListener(queues = "${app.rabbitmq.purchase.validation-queue:platon.products.purchase.validation}")
    public Message validate(Message message) {
        try {
            var request = objectMapper.readValue(message.getBody(), PurchaseAvailabilityRequest.class);
            if (request == null) {
                throw new AmqpRejectAndDontRequeueException("La solicitud de disponibilidad no puede ser null");
            }
            var response = availabilityService.check(request);
            log.info("Disponibilidad de compra {}: {}", request.requestId(), response.reason());
            // Spring envia este evento al exchange/routing key indicado en replyTo,
            // conservando el correlationId para que Payments identifique la respuesta.
            return MessageBuilder.withBody(objectMapper.writeValueAsBytes(response))
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .setType(response.eventType())
                    .build();
        } catch (IOException ex) {
            throw new AmqpRejectAndDontRequeueException("Evento de disponibilidad JSON invalido", ex);
        }
    }
}
