package com.mahesh.managementapi.dto.response;

import com.mahesh.managementapi.vo.OrderVo;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class OrderStatusResponse {

    private String statusCode;
    private String message;
    private String requestUUID;
    private OrderVo orderDetails;
}
