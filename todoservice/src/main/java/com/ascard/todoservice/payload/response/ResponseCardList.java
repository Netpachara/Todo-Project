package com.ascard.todoservice.payload.response;

import lombok.Data;

import java.util.List;

@Data
public class ResponseCardList {


    private List<ResponseCardDetails> responseCardList;

    private Integer totalItem;

}
