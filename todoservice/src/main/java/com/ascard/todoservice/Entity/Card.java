package com.ascard.todoservice.Entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "Card")
public class Card {

    @OneToOne(mappedBy = "card", fetch = FetchType.LAZY)
    private UserCard userCard;


    @Id
    @Column(name = "cardid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cardID;

    @Column(name = "cardname")
    private String cardName;

    @Column(name = "carddetails")
    private String cardDetails;

    @Column(name = "status")
    private String status;

    @Column(name = "createat")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @Column(name = "updateat")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateAt;

    @Column(name = "createbyuserid")
    private Integer createByUserID;




}
