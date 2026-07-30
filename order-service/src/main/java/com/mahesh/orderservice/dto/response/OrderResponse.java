package com.mahesh.orderservice.dto.response;

import com.mahesh.orderservice.vo.OrderVo;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class OrderResponse {
    private String statusCode;
    private String statusMessage;
    private String requestUUID;

    private OrderVo data;

}
