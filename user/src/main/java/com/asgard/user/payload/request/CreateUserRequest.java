package com.asgard.user.payload.request;

import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.*;

@Data
public class CreateUserRequest {

    @NotNull(message = "fullName is required")
    @NotBlank(message = "fullName is required")
    @NotEmpty(message = "fullName is required")
    @Size(min = 1, message = "fullName must greater than 1 characters")
    private String fullName;

    @NotNull(message = "email is required")
    @NotBlank(message = "email is required")
    @NotEmpty(message = "email is required")
    private String email;

    @NotNull(message = "password is required")
    @NotBlank(message = "password is required")
    @NotEmpty(message = "password is required")
    @Size(min = 8, message = "password must greater than 8 characters")
    private String password;

}
