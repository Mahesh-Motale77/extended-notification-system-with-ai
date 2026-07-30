package com.mahesh.orderservice.vo;

import com.mahesh.orderservice.model.Order;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderVo {

    private String orderId;
    private String userId;
    private String items;
    private Double amount;

    @Enumerated(EnumType.STRING)
    private Order.OrderStatus orderStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
