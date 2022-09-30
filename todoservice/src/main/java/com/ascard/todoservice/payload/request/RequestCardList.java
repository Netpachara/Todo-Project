package com.ascard.todoservice.payload.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class RequestCardList {

    private String search;

    private String cardStatus ;

    private Integer assignedUserID;

    private Boolean assigned;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date createdAtFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date createdAtTo;

    private Integer createdByUserID;

    @NotBlank(message = "sortBy is required")
    private String sortBy;

    private String sortDirection;

    @NotNull(message = "page is required")
    private Integer page;

    @NotNull(message = "pageSize is required")
    private Integer pageSize;

}
