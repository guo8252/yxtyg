package com.jscm.yxtyg.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UrgeRecordVO {
    private Long id;
    private Long requirementId;
    private String requirementName;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createTime;
}
