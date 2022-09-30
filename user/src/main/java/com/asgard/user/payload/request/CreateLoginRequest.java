package com.asgard.user.payload.request;


import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateLoginRequest {

    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "password is required")
    private String password;
}
