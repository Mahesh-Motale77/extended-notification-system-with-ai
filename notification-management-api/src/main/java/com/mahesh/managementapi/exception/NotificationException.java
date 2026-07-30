package com.mahesh.managementapi.exception;

import lombok.Getter;

@Getter
public class NotificationException extends RuntimeException{

    private final String errorCode;

    public NotificationException(String errorCode, String errorMessage){
        super(errorMessage);
        this.errorCode = errorCode;
    }
}
