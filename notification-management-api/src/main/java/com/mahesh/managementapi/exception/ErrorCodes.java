package com.mahesh.managementapi.exception;

public class ErrorCodes {
    public static final String NOT_FOUND = "404";
    public static final String BAD_REQUEST = "400";
    public static final String INTERNAL_ERROR = "500";
    public static final String DLQ_NOT_FOUND = "DLQ_001";
    public static final String INVALID_STATUS = "DLQ_002";
    public static final String NO_PAYLOAD = "DLQ_003";
    public static final String RETRY_FAILED = "DLQ_004";
    public static final String ORDER_NOT_FOUND = "ORD_005";
    public static final String TEMPLATE_NOT_FOUND = "TMT_006";
    public static final String NOTIFICATION_NOT_FOUND = "NTF_007";

}