package com.ascard.todoservice.payload.response;

import lombok.Data;

import java.util.List;

@Data
public class ResponseUserDetails {

    private Integer userID;
    private String fullName;
    private String email;

    private List<ResponseRole> responseRoleList;

    private Integer cardID;
}
