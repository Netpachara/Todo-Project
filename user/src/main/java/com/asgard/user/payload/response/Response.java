package com.asgard.user.payload.response;

import com.asgard.user.controller.UserController;
import com.asgard.user.entity.User;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import org.springframework.ui.Model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Response {

    Map<String, Object> details = new LinkedHashMap<>();

    @JsonAnySetter
    public void textResult(User rlt){
        this.details.put("data", rlt);
    }

    public Map<String, Object> getDetails(){
        return this.details;
    }
}
