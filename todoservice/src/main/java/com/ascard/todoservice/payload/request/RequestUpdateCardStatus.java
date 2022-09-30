package com.ascard.todoservice.payload.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RequestUpdateCardStatus {

    @NotNull(message = "userID is required")
    private Integer userID;

    @NotNull(message = "cardID is required")
    private Integer cardID;
}
