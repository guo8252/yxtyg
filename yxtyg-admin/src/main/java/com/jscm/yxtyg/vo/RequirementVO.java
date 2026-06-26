package com.jscm.yxtyg.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RequirementVO {
    private Long id;
    private String name;
    private String description;
    private Long productManagerId;
    private String productManagerName;
    private String systemName;
    private BigDecimal initialWorkload;
    private BigDecimal initialAmount;
    private BigDecimal finalWorkload;
    private BigDecimal reducedWorkload;
    private String status;
    private String statusLabel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
