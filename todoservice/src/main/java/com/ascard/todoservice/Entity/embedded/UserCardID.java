package com.ascard.todoservice.Entity.embedded;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserCardID implements Serializable {

    private Integer userID;
    private Integer cardID;
}
