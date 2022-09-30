package com.ascard.todoservice.controller;
import com.ascard.todoservice.Exception.TokenException;
import com.ascard.todoservice.payload.response.ResponseBase;
import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class ControllerAdvicer {

    @ExceptionHandler(MethodArgumentNotValidException.class)  //Request Body
    public ResponseEntity<ResponseBase> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception){
        List<String> errors = new ArrayList<>();
        for (FieldError fieldError : exception.getFieldErrors()) {
            System.out.println("MethodArgumentNotValid Exception " +fieldError.getDefaultMessage());
            errors.add(fieldError.getDefaultMessage());
        }
        ResponseBase responseBase = new ResponseBase();
        responseBase.setErrors(errors);
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

    @ExceptionHandler(TokenException.class)
    public ResponseEntity handleTokenException(TokenException ex){
        ResponseBase responseBase = new ResponseBase();
        List<String> errors = new ArrayList<>();
        errors.add(String.valueOf(ex));
        responseBase.setErrors(errors);
        return new ResponseEntity(responseBase, HttpStatus.UNAUTHORIZED);
    }







}
