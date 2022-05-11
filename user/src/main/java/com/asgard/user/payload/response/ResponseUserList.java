package com.asgard.user.payload.response;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class ResponseUserList {

    @Id
    @Column(name = "userid")
    private Integer userID ;

    @Column(name = "fullname")
    private String fullName;

    @Column(name = "email")
    private String email ;

    @Column(name = "password")
    private String password;

    @Column(name = "roleid")
    private Integer roleID;

    @Column(name = "title")
    private String title;

}
