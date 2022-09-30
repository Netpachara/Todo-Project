package com.ascard.todoservice.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class RequestEditCardDetails {

    @NotNull(message = "userID is required")
    private Integer userID;

    @NotNull(message = "cardID is required")
    private Integer cardID;

    private Integer userAssignedID;

    @NotBlank(message = "cardName is required")
    @Size(max = 10, message = "cardName must be less than 10 character")
    private String cardName;

    @NotBlank(message = "cardDetails is required")
    @Size(max = 100, message = "cardDetails must be less than 100 character")
    private String cardDetails;

}
