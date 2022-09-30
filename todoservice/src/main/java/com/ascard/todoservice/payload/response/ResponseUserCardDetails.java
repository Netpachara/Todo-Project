package com.ascard.todoservice.payload.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class ResponseUserCardDetails {

//    private Integer userID;
//    private String fullName;
//    private String email;

//    private List<ResponseCardDetails> responseCardDetails;

    private String cardName;
    private String cardDetails;
    private String status;
    private Date createAt;
    private Date updateAt;

    private Integer createByUserId;
    private String createByUserName;

    private Integer AssignedUserId;
    private String AssignedUserName;


}
