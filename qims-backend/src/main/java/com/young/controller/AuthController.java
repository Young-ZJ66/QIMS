package com.young.controller;

import com.young.common.Result;
import com.young.pojo.SysClient;
import com.young.pojo.SysUser;
import com.young.mapper.SysClientMapper;
import com.young.mapper.SysUserMapper;
import com.young.utils.JwtUtils;
import com.young.utils.MD5Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证授权控制器
 */
@Api(tags = "认证授权接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysClientMapper sysClientMapper;

    /**
     * 统一登录接口（自动识别 内部用户 和 客户）
     */
    @ApiOperation("统一登录接口")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        String md5Password = MD5Utils.encrypt(password); // 统一对输入密码进行MD5加密

        // 1. 尝试从内部用户表 (管理员、质检员) 查询
        List<SysUser> users = sysUserMapper.selectAll();
        SysUser user = users.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(md5Password))
                .findFirst()
                .orElse(null);

        if (user != null) {
            if (user.getStatus() != null && user.getStatus() == 0) {
                return Result.error("该账号已被禁用");
            }
            String token = JwtUtils.generateToken(user.getId(), user.getUsername(), user.getRoleId());
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("realName", user.getRealName());
            data.put("roleId", user.getRoleId()); // 1-管理员 2-检测员
            return Result.success(data);
        }

        // 2. 如果内部用户没找到，尝试从客户表查询
        List<SysClient> clients = sysClientMapper.selectAll();
        SysClient client = clients.stream()
                .filter(c -> c.getLoginAccount().equals(username) && c.getLoginPassword().equals(md5Password))
                .findFirst()
                .orElse(null);

        if (client != null) {
            // roleId 约定为 3 代表客户
            String token = JwtUtils.generateToken(client.getId(), client.getLoginAccount(), 3);
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userId", client.getId());
            data.put("username", client.getLoginAccount());
            data.put("realName", client.getCompanyName());
            data.put("roleId", 3);
            return Result.success(data);
        }

        return Result.error("用户名或密码错误");
    }

    /**
     * 客户注册接口
     */
    @ApiOperation("客户注册接口")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody SysClient client) {
        // 校验账号是否已存在
        List<SysClient> clients = sysClientMapper.selectAll();
        boolean exists = clients.stream().anyMatch(c -> c.getLoginAccount().equals(client.getLoginAccount()));
        if (exists) {
            return Result.error("该登录账号已被注册，请更换一个");
        }

        // 密码加密并保存
        client.setLoginPassword(MD5Utils.encrypt(client.getLoginPassword()));
        client.setCreateTime(LocalDateTime.now());
        sysClientMapper.insert(client);

        return Result.success();
    }
}
