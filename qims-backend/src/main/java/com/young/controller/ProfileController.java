package com.young.controller;

import com.young.common.Result;
import com.young.mapper.SysClientMapper;
import com.young.mapper.SysUserMapper;
import com.young.pojo.SysClient;
import com.young.pojo.SysUser;
import com.young.utils.PasswordUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 个人中心接口
 */
@Tag(name = "个人中心")
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysClientMapper sysClientMapper;

    /**
     * 获取个人信息
     */
    @Operation(summary = "获取个人信息")
    @GetMapping
    public Result<Map<String, Object>> getProfile(HttpServletRequest request) {
        Object roleIdObj = request.getAttribute("roleId");
        Object userIdObj = request.getAttribute("userId");
        if (roleIdObj == null || userIdObj == null) {
            Result<Map<String, Object>> error = Result.error("未获取到用户身份信息");
            error.setCode(401);
            return error;
        }

        String roleId = String.valueOf(roleIdObj);
        Long userId = Long.valueOf(String.valueOf(userIdObj));

        Map<String, Object> data = new HashMap<>();
        data.put("roleId", roleId);

        if ("2".equals(roleId)) {
            SysUser user = sysUserMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("realName", user.getRealName());
            data.put("phone", user.getPhone());
            return Result.success(data);
        }

        if ("3".equals(roleId)) {
            SysClient client = sysClientMapper.selectById(userId);
            if (client == null) {
                return Result.error("客户不存在");
            }
            data.put("id", client.getId());
            data.put("companyName", client.getCompanyName());
            data.put("contactPerson", client.getContactPerson());
            data.put("phone", client.getPhone());
            data.put("address", client.getAddress());
            data.put("loginAccount", client.getLoginAccount());
            return Result.success(data);
        }

        return Result.error("管理员不提供个人中心功能");
    }

    /**
     * 更新个人信息
     */
    @Operation(summary = "更新个人信息")
    @PutMapping
    public Result<Void> updateProfile(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Object roleIdObj = request.getAttribute("roleId");
        Object userIdObj = request.getAttribute("userId");
        if (roleIdObj == null || userIdObj == null) {
            Result<Void> error = Result.error("未获取到用户身份信息");
            error.setCode(401);
            return error;
        }

        String roleId = String.valueOf(roleIdObj);
        Long userId = Long.valueOf(String.valueOf(userIdObj));

        if ("2".equals(roleId)) {
            String realName = body.get("realName");
            if (realName == null) {
                return Result.success();
            }
            SysUser update = new SysUser();
            update.setId(userId);
            update.setRealName(realName);
            sysUserMapper.update(update);
            return Result.success();
        }

        if ("3".equals(roleId)) {
            SysClient update = new SysClient();
            update.setId(userId);
            update.setCompanyName(body.get("companyName"));
            update.setContactPerson(body.get("contactPerson"));
            update.setPhone(body.get("phone"));
            update.setAddress(body.get("address"));
            sysClientMapper.update(update);
            return Result.success();
        }

        return Result.error("管理员不支持更新个人信息");
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码")
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Object roleIdObj = request.getAttribute("roleId");
        Object userIdObj = request.getAttribute("userId");
        if (roleIdObj == null || userIdObj == null) {
            Result<Void> error = Result.error("未获取到用户身份信息");
            error.setCode(401);
            return error;
        }

        String roleId = String.valueOf(roleIdObj);
        Long userId = Long.valueOf(String.valueOf(userIdObj));

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return Result.error("参数不完整");
        }

        if ("2".equals(roleId)) {
            SysUser user = sysUserMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            // 验证旧密码
            if (!PasswordUtils.verify(oldPassword, user.getPassword())) {
                return Result.error("原密码不正确");
            }
            SysUser update = new SysUser();
            update.setId(userId);
            update.setPassword(PasswordUtils.hash(newPassword));
            sysUserMapper.update(update);
            log.info("用户 {} 修改了密码", userId);
            return Result.success();
        }

        if ("3".equals(roleId)) {
            SysClient client = sysClientMapper.selectById(userId);
            if (client == null) {
                return Result.error("客户不存在");
            }
            // 验证旧密码
            if (!PasswordUtils.verify(oldPassword, client.getLoginPassword())) {
                return Result.error("原密码不正确");
            }
            SysClient update = new SysClient();
            update.setId(userId);
            update.setLoginPassword(PasswordUtils.hash(newPassword));
            sysClientMapper.update(update);
            log.info("客户 {} 修改了密码", userId);
            return Result.success();
        }

        return Result.error("管理员不支持修改密码");
    }
}
