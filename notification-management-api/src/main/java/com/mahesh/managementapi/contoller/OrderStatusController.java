package com.mahesh.managementapi.contoller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mahesh.managementapi.dto.request.OrderStatusRequest;
import com.mahesh.managementapi.service.OrderStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order Status API", description = "Change order status and trigger notifications")
@RestController
@Data
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "order/api")
public class OrderStatusController {

    private final OrderStatusService orderStatusService;

    @Operation(
            summary = "Change order status",
            description = "Updates order status and re-publishes event to Kafka — triggers notification for new status"
    )
    @PostMapping(value = "/v1/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeStatus(@RequestBody OrderStatusRequest orderStatusRequest) throws JsonProcessingException {
        log.info("Inside OrderStatusController -> changeStatus() : OrderStatusRequest : {}", orderStatusRequest);
        return ResponseEntity.ok(orderStatusService.changeStatus(orderStatusRequest));
    }


}
