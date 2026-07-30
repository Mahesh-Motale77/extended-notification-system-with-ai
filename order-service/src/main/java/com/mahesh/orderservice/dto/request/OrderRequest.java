package com.mahesh.orderservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class OrderRequest {

    @NotBlank(message = "UserId should not be blank")
    private String userId;

    @NotBlank(message = "items should not be blank")
    private String items;

    @NotBlank(message = "amount should not be blank")
    private Double amount;
}
