package com.jscm.yxtyg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.dto.UserDTO;
import com.jscm.yxtyg.dto.UserQueryDTO;
import com.jscm.yxtyg.entity.User;
import com.jscm.yxtyg.exception.BusinessException;
import com.jscm.yxtyg.mapper.UserMapper;
import com.jscm.yxtyg.service.UserService;
import com.jscm.yxtyg.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserVO getByUsername(String username) {
        User user = this.baseMapper.selectByUsername(username);
        if (user == null) {
            return null;
        }
        return toVO(user);
    }

    @Override
    public PageResult<UserVO> queryPage(UserQueryDTO queryDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getUsername())) {
            wrapper.like(User::getUsername, queryDTO.getUsername());
        }
        if (StringUtils.hasText(queryDTO.getRealName())) {
            wrapper.like(User::getRealName, queryDTO.getRealName());
        }
        if (StringUtils.hasText(queryDTO.getRole())) {
            wrapper.eq(User::getRole, queryDTO.getRole());
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<User> result = this.page(page, wrapper);
        List<UserVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    @Override
    public void create(UserDTO dto) {
        User existing = this.baseMapper.selectByUsername(dto.getUsername());
        if (existing != null) {
            throw new BusinessException("账号已存在");
        }
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        this.save(user);
    }

    @Override
    public void update(Long id, UserDTO dto) {
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        BeanUtils.copyProperties(dto, user, "password");
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setId(id);
        this.updateById(user);
    }

    @Override
    public void delete(Long id) {
        this.removeById(id);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
