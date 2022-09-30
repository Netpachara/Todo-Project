package com.ascard.todoservice.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class RequestCreatedCard {

    private Integer userID; // Who assigned

    @NotBlank(message = "cardName is required")
    @Size(max = 10, message = "cardName must be less than 10 characters")
    private String cardName;


    @NotBlank(message = "cardDetails is required")
    @Size(max = 100, message = "cardDetails must be less than 100 characters")
    private String cardDetails;

    @NotNull(message = "createByUserID is required")
    private Integer createByUserID; //Who assign
}
