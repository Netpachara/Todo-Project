package com.asgard.user.payload.response;

import com.asgard.user.entity.embeddedid.UserRoleId;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class ResponseUserList {

    @EmbeddedId
    private UserRoleId userRoleId;

    @Column(name = "fullname")
    private String fullName;

    @Column(name = "email")
    private String email ;

    @Column(name = "password")
    private String password;

    @Column(name = "title")
    private String title;

}
