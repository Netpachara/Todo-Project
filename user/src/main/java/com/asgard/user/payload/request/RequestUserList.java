package com.asgard.user.payload.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RequestUserList {

    private String search;

    private Integer roleID;

    @NotNull(message = "page is required")
    private Integer page;

    @NotNull(message = "pageSize is required")
    private Integer pageSize;
}
