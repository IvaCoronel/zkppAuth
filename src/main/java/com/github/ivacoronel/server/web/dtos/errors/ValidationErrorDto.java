package com.github.ivacoronel.server.web.dtos.errors;

import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@ApiModel("ValidationError")
@Getter
@Builder
public class ValidationErrorDto implements Serializable {
    private static final long serialVersionUID = 6692364309366067411L;

    private String fieldName;

    private String errorCode;

    private Object rejectedValue;

    @Singular
    private List<Object> params;

    private String message;
}