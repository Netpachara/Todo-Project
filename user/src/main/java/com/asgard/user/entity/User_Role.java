package com.asgard.user.entity;


import com.asgard.user.entity.embeddedid.UserRoleId;
import lombok.Data;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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

    public User_Role(){

    }

    public User_Role(UserRoleId userRoleId) {
        this.userRoleId = userRoleId;
    }

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


