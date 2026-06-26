package com.jscm.yxtyg.controller;

import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.entity.User;
import com.jscm.yxtyg.mapper.UserMapper;
import com.jscm.yxtyg.security.CurrentUser;
import com.jscm.yxtyg.security.JwtTokenProvider;
import com.jscm.yxtyg.service.UserService;
import com.jscm.yxtyg.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Result.error("账号或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() == 0) {
            return Result.error("账号已禁用");
        }
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        UserVO userVO = userService.getByUsername(user.getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", userVO);
        return Result.success(data);
    }

    @GetMapping("/me")
    public Result<UserVO> me(@AuthenticationPrincipal CurrentUser currentUser) {
        UserVO user = userService.getByUsername(currentUser.getUsername());
        return Result.success(user);
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
