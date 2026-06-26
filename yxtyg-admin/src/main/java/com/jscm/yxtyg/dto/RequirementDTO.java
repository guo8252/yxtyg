package com.jscm.yxtyg.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RequirementDTO {
    private Long id;
    private String name;
    private String description;
    private Long productManagerId;
    private String systemName;
    private BigDecimal initialWorkload;
    private BigDecimal initialAmount;
    private BigDecimal finalWorkload;
    private String status;
}
