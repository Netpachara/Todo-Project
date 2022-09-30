package com.ascard.todoservice.controller;

import com.ascard.todoservice.Annotation.BypassAuthentication;
import com.ascard.todoservice.Annotation.NetLog;
import com.ascard.todoservice.payload.response.ResponseBase;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@RestController
public class NewController {

    @GetMapping("/card/date")
    @NetLog
    public ResponseBase testFormatDate(@Valid @Param("date") String date) throws ParseException {
        System.out.println("Middle");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        ResponseBase res = new ResponseBase();
        List<String> error = new ArrayList<>();
        try{
            simpleDateFormat.parse(date);
        }
        catch (ParseException e){
            error.add(e.getMessage());
            res.setErrors(error);
        }
        return res;
    }

    @GetMapping("/card/byPass")
    @NetLog
    @BypassAuthentication
    public ResponseEntity checkBypass(){
        System.out.println("Bypass...");
        return ResponseEntity.ok().build();
    }
}
