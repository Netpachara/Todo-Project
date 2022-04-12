package com.asgard.user.entity;


import lombok.Data;

import javax.persistence.*;

import com.asgard.user.entity.embeddedid.UserRoleId;

@Data
@Entity
@Table(name  = "User_Role")
//@IdClass(UserRoleId.class)
public class User_Role {

    @EmbeddedId
    private UserRoleId userRoleId;

    @ManyToOne()
    @JoinColumn(name="userID", insertable = false, updatable = false)
    private User user;

    @Override
    public String toString() {
        return "User_Role{" +
                "userRoleId=" + userRoleId +
                '}';
    }

    @ManyToOne()
    @JoinColumn(name="roleID", insertable = false, updatable = false)
    private Role role;

//    @Id
//    @Column(name = "userid")
//    Integer userID;
//
//    @Id
//    @Column(name = "roleid")
//    Integer roleID;
}


