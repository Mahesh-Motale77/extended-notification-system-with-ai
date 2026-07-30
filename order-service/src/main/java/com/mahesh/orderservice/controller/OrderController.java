package com.mahesh.orderservice.controller;

import com.mahesh.orderservice.dto.request.OrderRequest;
import com.mahesh.orderservice.dto.response.OrderResponse;
import com.mahesh.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order API", description = "Create orders and publish to Kafka")
@Data
@RestController
@RequestMapping(value = "/order/v1")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Create Order",
            description = "Creates a new order, saves to DB and publishes ORDER_CREATED event to Kafka"
    )
    @PostMapping(value = "/create")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderRequest orderRequest){
        log.info("Inside OrderController --> createOrder() : orderRequest : {}", orderRequest);
        return ResponseEntity.ok(orderService.createOrder(orderRequest));
    }


}
