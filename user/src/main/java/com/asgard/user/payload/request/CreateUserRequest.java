package com.asgard.user.payload.request;

import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.*;

@Data
public class CreateUserRequest {

    @NotNull(message = "fullName is required")
    @NotBlank(message = "fullName is required")
    @NotEmpty(message = "fullName is required")
    @Size(min = 1, message = "fullName must greater than 1 character")
    private String fullName;

    @NotNull
    @NotBlank
    @NotEmpty

    private String email;

    @NotNull
    @NotBlank
    @NotEmpty
    @Size(min = 8)
    private String password;

}
