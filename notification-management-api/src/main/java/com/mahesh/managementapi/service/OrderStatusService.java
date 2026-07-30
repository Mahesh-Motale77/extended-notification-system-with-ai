package com.mahesh.managementapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mahesh.managementapi.dto.request.OrderStatusRequest;
import com.mahesh.managementapi.dto.response.OrderStatusResponse;

public interface OrderStatusService {
    OrderStatusResponse changeStatus(OrderStatusRequest orderStatusRequest) throws JsonProcessingException;
}
