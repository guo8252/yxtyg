package com.jscm.yxtyg.dto;

import lombok.Data;

@Data
public class PageQueryDTO {
    private Long current = 1L;
    private Long size = 10L;
}
