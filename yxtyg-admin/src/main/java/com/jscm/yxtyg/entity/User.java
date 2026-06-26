package com.jscm.yxtyg.entity;

import com.jscm.yxtyg.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class User extends BaseEntity {

    private String username;
    private String realName;
    private String password;
    private String role;
    private Integer status;

    @TableLogic
    private Integer deleted;
}
