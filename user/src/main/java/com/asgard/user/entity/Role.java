package com.asgard.user.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Data
@Entity
@Table(name  = "[Role]")
public class Role {

    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER)
    private List<User_Role> user_role;


    @Id
    @Column(name = "roleid")
    private Integer roleID;

    @Column(name = "title")
    private String title;

}
