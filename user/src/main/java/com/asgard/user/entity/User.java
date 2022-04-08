package com.asgard.user.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name  = "[User]")
public class User {

    @Id
    @Column(name = "userid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userID;

    @Column(name = "fullname")
    private String fullName;


    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;


}
