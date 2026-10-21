package edu.cibertec.products.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cibertec.products.dto.PaymentRejectedEvent;
import edu.cibertec.products.service.StockReturnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRejectedListener {
    private final ObjectMapper mapper;
    private final StockReturnService stockReturn;

    @RabbitListener(queues = "platon.products.payment.rejected", containerFactory = "stockReturnListenerFactory")
    public void receive(Message message) {
        try {
            var event = mapper.readValue(message.getBody(), PaymentRejectedEvent.class);
            if (event == null || event.eventId() == null || !"payment.rejected".equals(event.eventType())
                    || event.stockDeductionId() == null || event.idProducts() == null || event.idProducts() <= 0
                    || event.numberSold() == null || event.numberSold() <= 0) {
                throw new IllegalArgumentException("Evento payment.rejected invalido");
            }
            stockReturn.restore(event);
            log.info("Devolucion de stock procesada: {}", event.stockDeductionId());
        } catch (IOException | IllegalArgumentException ex) {
            throw new AmqpRejectAndDontRequeueException("Evento de devolucion invalido", ex);
        }
    }
}
