package com.github.ivacoronel.server.web.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ivacoronel.server.domain.model.types.SessionStatus;
import com.github.ivacoronel.server.web.dtos.audit.AuditableDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.validation.constraints.NotEmpty;

@ApiModel("User")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto extends AuditableDto {
    private static final long serialVersionUID = 5762617605382814204L;

    @ApiModelProperty(required = true)
    private Long id;

    @ApiModelProperty(required = true)
    @NotEmpty
    private String name;

    @ApiModelProperty(required = true)
    @NotNull
    private String passwordless;

    @ApiModelProperty(required = false)
    @JsonIgnore
    private String secret;
    
    @ApiModelProperty(required = false)
    @JsonIgnore
    private SessionStatus sstatus;
}