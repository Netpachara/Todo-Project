package com.ascard.todoservice.payload.response;

import lombok.Data;

import java.util.Date;

@Data
public class ResponseCardDetails {

    private Integer cardID;
    private String cardName;
    private String cardDetails;
    private String status;

    private Date createdAt;
    private String createdUserFullName;
    private String assignedUserFullName;

}
