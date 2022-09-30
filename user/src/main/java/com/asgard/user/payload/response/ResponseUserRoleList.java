package com.asgard.user.payload.response;

import lombok.Data;


@Data
public class ResponseUserRoleList {

    private Integer userID ;

    private String fullName;

    private String email ;

    private String title;


}
