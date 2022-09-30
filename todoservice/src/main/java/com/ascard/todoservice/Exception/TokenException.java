package com.ascard.todoservice.Exception;

import lombok.Data;

@Data
public class TokenException extends Throwable {

    private String code;

    private String message;


    public TokenException(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
