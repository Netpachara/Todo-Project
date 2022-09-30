package com.asgard.user.entity.embeddedid;


import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
public class UserRoleId implements Serializable {


    private Integer userID;
    private Integer roleID;

}
