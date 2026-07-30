package com.mahesh.orderservice.exception;

import lombok.Getter;

@Getter
public class OrderException extends RuntimeException{

    private final String errorCode;

    public OrderException(String errorCode, String errorMessage){
        super(errorMessage);
        this.errorCode = errorCode;
    }

}
