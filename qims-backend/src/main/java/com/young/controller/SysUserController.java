package com.young.controller;

import com.young.pojo.SysUser;
import com.young.service.SysUserService;
import com.young.common.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统用户接口
 */
@Tag(name = "系统用户")
@RestController
@RequestMapping("/api/sys-user")
public class SysUserController {

    @Autowired
    private SysUserService service;

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Void> add(@RequestBody SysUser record) {
        service.add(record);
        return Result.success();
    }

    @Operation(summary = "修改用户")
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

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }

    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    /**
     * 查询所有用户（支持分页）
     * <p>
     * 当提供 page 和 pageSize 参数时返回分页结果，否则返回全部。
     * 分页响应格式：{ list: [...], total: N, page: P, pageSize: S }
     * </p>
     *
     * @param page     页码（从1开始），可选
     * @param pageSize 每页条数，可选
     */
    @Operation(summary = "查询用户列表")
    @GetMapping
    public Result<Object> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        // 无分页参数时，返回全部
        if (page == null || pageSize == null) {
            return Result.success(service.getAll());
        }

        // 使用 PageHelper 分页
        PageHelper.startPage(page, pageSize);
        List<SysUser> list = service.getAll();
        PageInfo<SysUser> pageInfo = new PageInfo<>(list);

        // 组装分页响应
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("list", pageInfo.getList());
        pageResult.put("total", pageInfo.getTotal());
        pageResult.put("page", pageInfo.getPageNum());
        pageResult.put("pageSize", pageInfo.getPageSize());
        pageResult.put("pages", pageInfo.getPages()); // 总页数

        return Result.success(pageResult);
    }
}
