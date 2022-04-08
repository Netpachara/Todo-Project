package com.asgard.user.entity.embeddedid;


import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class UserRoleId implements Serializable {
    private Integer userID;
    private Integer roleID;

}
