package com.mahesh.managementapi.contoller;

import com.mahesh.managementapi.dto.response.DLQRetryResponse;
import com.mahesh.managementapi.dto.response.NotificationDetailsResponse;
import com.mahesh.managementapi.service.DLQRetryService;
import com.mahesh.managementapi.service.DLQService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "DLQ API", description = "Manage failed notifications in Dead Letter Queue")
@RestController
@Data
@RequestMapping(value = "/dlq/api")
@Slf4j
@RequiredArgsConstructor
public class DLQContoller {

    private final DLQService dlqService;
    private final DLQRetryService dlqRetryService;

    @Operation(
            summary = "Get all DLQ records",
            description = "Returns all notifications that failed after 3 retry attempts"
    )
    @GetMapping(value = "/v1/records", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotificationDetailsResponse> getAllDlqNotifications(){
        log.info("Inside DLQContoller --> getAllDlqNotifications()");
        return ResponseEntity.ok(dlqService.getAllDlqNotifications());
    }

    @Operation(
            summary = "Retry DLQ record",
            description = "Clears Redis idempotency key and re-publishes event to order-event-topic for reprocessing"
    )
    @PostMapping(value = "/v1/retry/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DLQRetryResponse> retryDLQRecords(@PathVariable("orderId") String orderId){
        log.info("Inside DLQContoller --> retryDLQRecords()");
        return ResponseEntity.ok(dlqRetryService.retryDLQRecords(orderId));
    }


}
