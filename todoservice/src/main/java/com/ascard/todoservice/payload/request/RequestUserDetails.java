package com.ascard.todoservice.payload.request;

import lombok.Data;

@Data
public class RequestUserDetails {

    private Integer userID;
    private String fullName;
    private String email;
}
