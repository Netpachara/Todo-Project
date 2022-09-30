package com.ascard.todoservice.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Data
@Entity
public class ResponseUserCardList {

    @Id
    @Column(name = "cardid")
    private Integer cardID;

    @Column(name = "cardname")
    private String cardName;

    @Column(name = "carddetails")
    private String cardDetails;

    @Column(name = "status")
    private String status;

    @Column(name = "createat")
    private Date createAt ;

    @Column(name = "updateat")
    private Date updateAt ;

    @Column(name = "createbyuserid")
    private Integer createByUserID;

    @Column(name = "userassignedid")
    private Integer UserAssignedID;

    @Column(name = "cardassignedid")
    private Integer CardAssignedID;
    

}
