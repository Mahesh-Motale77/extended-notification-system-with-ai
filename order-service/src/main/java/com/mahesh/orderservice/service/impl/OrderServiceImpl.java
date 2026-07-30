package com.mahesh.orderservice.service.impl;

import com.mahesh.orderservice.dto.request.OrderRequest;
import com.mahesh.orderservice.dto.response.OrderResponse;
import com.mahesh.orderservice.kafka.OrderEventPublisherService;
import com.mahesh.orderservice.model.Order;
import com.mahesh.orderservice.repository.OrderRepository;
import com.mahesh.orderservice.service.OrderService;
import com.mahesh.orderservice.vo.OrderVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisherService orderEventPublisherService;

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Inside OrderServiceImpl -> createOrder() : OrderRequest : {}", request);

        // creating a new order with status as CREATED
        Order order = Order.builder()
                .orderId("ORD"+System.currentTimeMillis())
                .userId(request.getUserId())
                .orderStatus(Order.OrderStatus.ORDER_CREATED)
                .amount(request.getAmount())
                .items(request.getItems())
                .build();

        Order createdOrder = orderRepository.save(order);
        log.info("Inside OrderServiceImpl -> createOrder() : Order created for userId : {}", createdOrder.getUserId());

        // call kafka producer method to publish an event
        OrderVo orderVo = mapOrderEntityToOrderVo(createdOrder);

        CompletableFuture.runAsync(() -> orderEventPublisherService.publishEventForOrderCreated(orderVo));

        return OrderResponse.builder()
                .statusCode("200")
                .statusMessage("Order created successfully!")
                .requestUUID(MDC.get("UUID"))
                .data(orderVo)
                .build();
    }

    private OrderVo mapOrderEntityToOrderVo(Order createdOrder) {
        OrderVo orderVo= new OrderVo();

        orderVo.setOrderId(createdOrder.getOrderId());
        orderVo.setOrderStatus(createdOrder.getOrderStatus());
        orderVo.setAmount(createdOrder.getAmount());
        orderVo.setItems(createdOrder.getItems());
        orderVo.setUpdatedAt(createdOrder.getUpdatedAt());
        orderVo.setCreatedAt(createdOrder.getCreatedAt());
        orderVo.setUserId(createdOrder.getUserId());

        return orderVo;
    }
}
