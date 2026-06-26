package com.jscm.yxtyg.controller;

import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.entity.User;
import com.jscm.yxtyg.mapper.UserMapper;
import com.jscm.yxtyg.security.JwtTokenProvider;
import com.jscm.yxtyg.service.UserService;
import com.jscm.yxtyg.vo.UserVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthController authController;

    @Test
    public void login_success() {
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encoded-password");
        user.setRole("SYS_ADMIN");
        user.setStatus(1);
        when(userMapper.selectByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("123456", "encoded-password")).thenReturn(true);
        when(jwtTokenProvider.generateToken(1L, "admin", "SYS_ADMIN")).thenReturn("mock-token");

        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setUsername("admin");
        when(userService.getByUsername("admin")).thenReturn(userVO);

        Result<Map<String, Object>> result = authController.login(request);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("mock-token", result.getData().get("token"));
        assertSame(userVO, result.getData().get("user"));
    }

    @Test
    public void login_fail_wrong_password() {
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong-password");

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encoded-password");
        user.setRole("SYS_ADMIN");
        user.setStatus(1);
        when(userMapper.selectByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        Result<Map<String, Object>> result = authController.login(request);

        assertEquals(500, result.getCode());
        assertEquals("账号或密码错误", result.getMessage());
    }
}
