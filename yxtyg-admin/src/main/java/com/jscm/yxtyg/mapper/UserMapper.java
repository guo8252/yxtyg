package com.jscm.yxtyg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jscm.yxtyg.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper extends BaseMapper<User> {

    User selectByUsername(@Param("username") String username);
}
