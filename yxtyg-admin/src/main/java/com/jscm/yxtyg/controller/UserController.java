package com.jscm.yxtyg.controller;

import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.dto.UserDTO;
import com.jscm.yxtyg.dto.UserQueryDTO;
import com.jscm.yxtyg.service.UserService;
import com.jscm.yxtyg.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<PageResult<UserVO>> list(UserQueryDTO queryDTO) {
        return Result.success(userService.queryPage(queryDTO));
    }

    @GetMapping("/product-managers")
    public Result<PageResult<UserVO>> productManagers(UserQueryDTO queryDTO) {
        queryDTO.setRole("PRODUCT_MANAGER");
        return Result.success(userService.queryPage(queryDTO));
    }

    @PostMapping
    public Result<Void> create(@RequestBody UserDTO dto) {
        userService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UserDTO dto) {
        userService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
}
