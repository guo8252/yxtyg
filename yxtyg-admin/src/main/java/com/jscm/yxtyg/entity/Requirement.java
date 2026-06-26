package com.jscm.yxtyg.entity;

import com.jscm.yxtyg.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_requirement")
public class Requirement extends BaseEntity {

    private String name;
    private String description;
    private Long productManagerId;
    private String systemName;
    private BigDecimal initialWorkload;
    private BigDecimal initialAmount;
    private BigDecimal finalWorkload;
    private BigDecimal reducedWorkload;
    private String status;

    @TableLogic
    private Integer deleted;
}
