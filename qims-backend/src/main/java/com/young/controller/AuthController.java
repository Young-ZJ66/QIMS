package com.young.controller;

import com.young.common.Result;
import com.young.pojo.SysClient;
import com.young.pojo.SysUser;
import com.young.mapper.SysClientMapper;
import com.young.mapper.SysUserMapper;
import com.young.utils.JwtUtils;
import com.young.utils.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证授权控制器
 */
@Tag(name = "认证授权")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysClientMapper sysClientMapper;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 统一登录接口
     */
    @Operation(summary = "统一登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }

        // 1. 尝试从内部用户表查询
        SysUser user = sysUserMapper.selectByUsername(username);
        if (user != null) {
            if (user.getStatus() != null && user.getStatus() == 0) {
                return Result.error("该账号已被禁用");
            }

            if (PasswordUtils.verify(password, user.getPassword())) {
                String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRoleId());

                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("userId", user.getId());
                data.put("username", user.getUsername());
                data.put("realName", user.getRealName());
                data.put("roleId", user.getRoleId());
                return Result.success(data);
            }
        }

        // 2. 如果内部用户没找到，尝试从客户表查询
        SysClient client = sysClientMapper.selectByLoginAccount(username);
        if (client != null) {
            if (client.getStatus() != null && client.getStatus() == 0) {
                return Result.error("该客户账号已被禁用");
            }

            if (PasswordUtils.verify(password, client.getLoginPassword())) {
                // roleId 约定为 3 代表客户
                String token = jwtUtils.generateToken(client.getId(), client.getLoginAccount(), 3);

                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("userId", client.getId());
                data.put("username", client.getLoginAccount());
                data.put("realName", client.getCompanyName());
                data.put("roleId", 3);
                return Result.success(data);
            }
        }

        return Result.error("用户名或密码错误");
    }

    /**
     * 客户注册接口
     */
    @Operation(summary = "客户注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody SysClient client) {
        SysClient existing = sysClientMapper.selectByLoginAccount(client.getLoginAccount());
        if (existing != null) {
            return Result.error("该登录账号已被注册，请更换一个");
        }

        client.setLoginPassword(PasswordUtils.hash(client.getLoginPassword()));
        client.setCreateTime(LocalDateTime.now());
        client.setStatus(1);
        sysClientMapper.insert(client);

        return Result.success();
    }

    /**
     * Token 刷新接口
     * <p>
     * 当拦截器检测到 Token 即将过期时，
     * 前端可调用此接口获取新的 Token，实现静默续期。
     * </p>
     */
    @Operation(summary = "刷新Token")
    @PostMapping("/refresh-token")
    public Result<Map<String, Object>> refreshToken(jakarta.servlet.http.HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        Object usernameObj = request.getAttribute("username");
        Object roleIdObj = request.getAttribute("roleId");

        if (userIdObj == null || roleIdObj == null) {
            Result<Map<String, Object>> error = Result.error("未登录或 Token 已过期");
            error.setCode(401);
            return error;
        }

        Long userId = Long.valueOf(String.valueOf(userIdObj));
        String username = String.valueOf(usernameObj);
        Integer roleId = Integer.valueOf(String.valueOf(roleIdObj));

        String newToken = jwtUtils.generateToken(userId, username, roleId);

        Map<String, Object> data = new HashMap<>();
        data.put("token", newToken);
        return Result.success(data);
    }
}
