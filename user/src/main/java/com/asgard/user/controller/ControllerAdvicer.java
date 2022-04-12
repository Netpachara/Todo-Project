package com.asgard.user.controller;



import com.asgard.user.payload.response.ResponseBase;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class ControllerAdvicer {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {

        List<String> err = new ArrayList<>();
        for (FieldError fieldError : exception.getFieldErrors()) {
            err.add(fieldError.getDefaultMessage());
        }
        ResponseBase responseBase = new ResponseBase();
        responseBase.setErrors(err);
        return ResponseEntity.badRequest().body(responseBase);
    }
}
