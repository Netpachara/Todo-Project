package com.asgard.user.controller;



import com.asgard.user.payload.response.ResponseBase;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
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

    @ExceptionHandler(BindException.class) //Param
    public ResponseEntity<?> handleParamMissing(BindException ex) {
        ResponseBase responseBase = new ResponseBase();
        List<String> errors = new ArrayList<>();
        for(FieldError objectError : ex.getFieldErrors()){
            System.out.println("Bind Exception " + objectError.getDefaultMessage());
            errors.add(objectError.getDefaultMessage());
        }
        responseBase.setErrors(errors);
        return ResponseEntity.badRequest().body(responseBase);
    }
}
