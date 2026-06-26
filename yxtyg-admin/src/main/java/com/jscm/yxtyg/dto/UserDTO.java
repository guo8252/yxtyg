package com.jscm.yxtyg.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String realName;
    private String password;
    private String role;
    private Integer status;
}
