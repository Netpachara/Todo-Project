package com.asgard.user.payload.request;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class CreateLoginRequest {

    @NotNull(message = "email is required")
    @NotBlank(message = "email is required")
    @NotEmpty(message = "email is required")
    private String email;

    @NotNull(message = "password is required")
    @NotBlank(message = "password is required")
    @NotEmpty(message = "password is required")
    private String password;
}
