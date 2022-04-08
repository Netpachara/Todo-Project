package com.asgard.user.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "user")
public class Role {

    @Id
    @Column(name = "roleid")
    private Integer roleID;

    @Column(name = "title")
    private String title;
}
