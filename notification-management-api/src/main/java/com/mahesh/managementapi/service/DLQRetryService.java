package com.mahesh.managementapi.service;

import com.mahesh.managementapi.dto.response.DLQRetryResponse;

public interface DLQRetryService {

    DLQRetryResponse retryDLQRecords(String orderId);
}
