package com.jscm.yxtyg.entity;

import com.jscm.yxtyg.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_urge_record")
public class UrgeRecord extends BaseEntity {

    private Long requirementId;
    private Long operatorId;

    @TableLogic
    private Integer deleted;
}
