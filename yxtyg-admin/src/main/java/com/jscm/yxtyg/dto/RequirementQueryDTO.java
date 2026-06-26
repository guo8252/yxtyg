package com.jscm.yxtyg.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RequirementQueryDTO extends PageQueryDTO {
    private String name;
    private Long productManagerId;
    private String systemName;
    private String status;
}
