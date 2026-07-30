package com.mahesh.orderservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahesh.orderservice.vo.OrderVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisherService {

    private static final String ORDER_EVENT_TOPIC = "order-event-topic";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishEventForOrderCreated(OrderVo orderVo) {
        try {
            MDC.put("UUID", UUID.randomUUID().toString());

            log.info("Inside OrderEventPublisherService --> publishEventForOrderCreated() : OrderVo : {}",orderVo);
            String orderEvent = objectMapper.writeValueAsString(orderVo);

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(ORDER_EVENT_TOPIC, orderVo.getUserId(), orderEvent);

            future.whenComplete((result, exception) -> {
               if (exception == null){
                   log.info("Event pushed to kafka successfully for orderId : {} | userId : {} | topic : {} | partition : {} | offset : {}",
                           orderVo.getOrderId(), orderVo.getUserId(), ORDER_EVENT_TOPIC, result.getRecordMetadata().topic(), result.getRecordMetadata().offset());
               }
               else {
                   log.info("Event not pushed to kafka for orderId : {} | Exception : {}", orderVo.getOrderId(), exception.getMessage());
               }
            });
        } catch (Exception e) {
            log.info("Exception occured while sending event to kafka for OrderId : {} | Exception : {}", orderVo.getOrderId(), e.getMessage());
        }
    }
}
