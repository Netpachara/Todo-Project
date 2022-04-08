package com.asgard.user.entity;


import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import com.asgard.user.entity.embeddedid.UserRoleId;

@Data
@Entity
@Table(name = "user")
//@IdClass(UserRoleId.class)
public class UserRole {

    @EmbeddedId
    private UserRoleId userRoleId;

//    @Id
//    @Column(name = "userid")
//    Integer userID;
//
//    @Id
//    @Column(name = "roleid")
//    Integer roleID;
}


