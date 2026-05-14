package com.young.controller;

import com.young.pojo.SysUser;
import com.young.service.SysUserService;
import com.young.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Api(tags = "系统用户接口")
@RestController
@RequestMapping("/api/sys-user")
public class SysUserController {

    @Autowired
    private SysUserService service;

    @ApiOperation("新增")
    @PostMapping
    public Result<Void> add(@RequestBody SysUser record) {
        service.add(record);
        return Result.success();
    }

    @ApiOperation("修改")
    @PutMapping
    public Result<Void> update(@RequestBody SysUser record, jakarta.servlet.http.HttpServletRequest request) {
        Object currentUserIdObj = request.getAttribute("userId");
        if (currentUserIdObj != null) {
            Long currentUserId = Long.valueOf(String.valueOf(currentUserIdObj));
            if (currentUserId.equals(record.getId())) {
                // 禁止将自己的状态修改为0（禁用），也禁止修改自己的角色
                if (record.getStatus() != null && record.getStatus() == 0) {
                    return Result.error("不能禁用当前正在登录的账号");
                }
                if (record.getRoleId() != null && record.getRoleId() != 1) {
                    return Result.error("不能修改当前登录账号的角色");
                }
            }
        }
        service.update(record);
        return Result.success();
    }

    @ApiOperation("删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }

    @ApiOperation("根据ID查询")
    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @ApiOperation("查询所有")
    @GetMapping
    public Result<List<SysUser>> getAll() {
        return Result.success(service.getAll());
    }
}
