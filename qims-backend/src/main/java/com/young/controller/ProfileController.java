package com.young.controller;

import com.young.common.Result;
import com.young.mapper.SysClientMapper;
import com.young.mapper.SysUserMapper;
import com.young.pojo.SysClient;
import com.young.pojo.SysUser;
import com.young.utils.MD5Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "个人中心接口")
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysClientMapper sysClientMapper;

    @ApiOperation("获取个人信息")
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

    @ApiOperation("更新个人信息")
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
            // 客户账号不允许修改，因此注释掉对 loginAccount 的冲突校验
            // String loginAccount = body.get("loginAccount");
            // if (loginAccount != null) {
            //     List<SysClient> clients = sysClientMapper.selectAll();
            //     boolean exists = clients.stream().anyMatch(c -> !userId.equals(c.getId()) && loginAccount.equals(c.getLoginAccount()));
            //     if (exists) {
            //         return Result.error("该登录账号已被占用，请更换一个");
            //     }
            // }

            SysClient update = new SysClient();
            update.setId(userId);
            update.setCompanyName(body.get("companyName"));
            update.setContactPerson(body.get("contactPerson"));
            update.setPhone(body.get("phone"));
            update.setAddress(body.get("address"));
            // update.setLoginAccount(loginAccount); // 客户账号不允许修改
            sysClientMapper.update(update);
            return Result.success();
        }

        return Result.error("管理员不支持更新个人信息");
    }

    @ApiOperation("修改密码")
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

        String oldMd5 = MD5Utils.encrypt(oldPassword);
        String newMd5 = MD5Utils.encrypt(newPassword);

        if ("2".equals(roleId)) {
            SysUser user = sysUserMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            if (user.getPassword() == null || !user.getPassword().equals(oldMd5)) {
                return Result.error("原密码不正确");
            }
            SysUser update = new SysUser();
            update.setId(userId);
            update.setPassword(newMd5);
            sysUserMapper.update(update);
            return Result.success();
        }

        if ("3".equals(roleId)) {
            SysClient client = sysClientMapper.selectById(userId);
            if (client == null) {
                return Result.error("客户不存在");
            }
            if (client.getLoginPassword() == null || !client.getLoginPassword().equals(oldMd5)) {
                return Result.error("原密码不正确");
            }
            SysClient update = new SysClient();
            update.setId(userId);
            update.setLoginPassword(newMd5);
            sysClientMapper.update(update);
            return Result.success();
        }

        return Result.error("管理员不支持修改密码");
    }
}

