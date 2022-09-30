package com.asgard.user.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class CreateUserRequest {

    @NotNull(message = "fullName is required")
    @NotBlank(message = "fullName is required")
    @NotEmpty(message = "fullName is required")
    @Size(min = 1, message = "fullName must be greater than 1 characters")
    private String fullName;

    @NotNull(message = "email is required")
    @NotBlank(message = "email is required")
    @NotEmpty(message = "email is required")
    private String email;

    @NotNull(message = "password is required")
    @NotBlank(message = "password is required")
    @NotEmpty(message = "password is required")
    @Size(min = 8, message = "password must be greater than 8 characters")
    private String password;

}
