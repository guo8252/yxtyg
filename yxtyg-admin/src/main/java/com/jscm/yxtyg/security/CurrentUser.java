package com.jscm.yxtyg.security;

import lombok.Data;

@Data
public class CurrentUser {
    private Long userId;
    private String username;
    private String role;
}
