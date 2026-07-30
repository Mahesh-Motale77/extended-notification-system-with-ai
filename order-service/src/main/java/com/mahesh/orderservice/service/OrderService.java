package com.mahesh.orderservice.service;

import com.mahesh.orderservice.dto.request.OrderRequest;
import com.mahesh.orderservice.dto.response.OrderResponse;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

}