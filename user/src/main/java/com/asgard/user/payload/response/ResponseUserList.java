package com.asgard.user.payload.response;

import lombok.Data;

@Data
public class ResponseUserList {

    private Integer userID ;
    private String fullName;
    private String email ;

    private Integer roleID;
    private String title;

}
