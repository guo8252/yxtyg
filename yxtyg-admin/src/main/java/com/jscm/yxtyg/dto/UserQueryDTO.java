package com.jscm.yxtyg.dto;

import lombok.Data;

@Data
public class UserQueryDTO extends PageQueryDTO {
    private String username;
    private String realName;
    private String role;
}
