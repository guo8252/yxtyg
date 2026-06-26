package com.jscm.yxtyg.service;

import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.dto.UserDTO;
import com.jscm.yxtyg.dto.UserQueryDTO;
import com.jscm.yxtyg.vo.UserVO;

public interface UserService {
    UserVO getByUsername(String username);
    PageResult<UserVO> queryPage(UserQueryDTO queryDTO);
    void create(UserDTO dto);
    void update(Long id, UserDTO dto);
    void delete(Long id);
}
