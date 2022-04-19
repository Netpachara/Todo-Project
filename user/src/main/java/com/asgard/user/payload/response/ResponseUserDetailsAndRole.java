package com.asgard.user.payload.response;

import lombok.Data;

import java.util.List;

@Data
public class ResponseUserDetailsAndRole {

    private Integer userID ;
    private String fullName;
    private String email ;

    private List<ResponseRole> responseRoleList;

}


