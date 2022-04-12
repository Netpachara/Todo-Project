package com.asgard.user.entity;


import com.asgard.user.entity.embeddedid.UserRoleId;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name  = "[User]")
public class User {

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<User_Role> user_role;


    @Override
    public String toString() {
        return "User{" +
                "userID=" + userID +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

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
