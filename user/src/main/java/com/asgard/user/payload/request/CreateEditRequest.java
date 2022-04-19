package com.asgard.user.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CreateEditRequest {

    private Integer userID;

    @NotNull(message = "fullName is required")
    @NotBlank(message = "fullName is required")
    @NotEmpty(message = "fullName is required")
    private String fullName;

    @NotNull(message = "email is required")
    @NotBlank(message = "email is required")
    @NotEmpty(message = "email is required")
    private String email;

    private List<Integer> roleIDList;
}
