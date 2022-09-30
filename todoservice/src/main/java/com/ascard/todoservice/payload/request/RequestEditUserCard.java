package com.ascard.todoservice.payload.request;

import lombok.Data;

import java.util.List;

@Data
public class RequestEditUserCard {
    private Integer userID;
    private String fullName ;
    private String email;


    private List<Integer> cardList;

}
