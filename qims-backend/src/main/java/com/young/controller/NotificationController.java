package com.young.controller;

import com.young.common.Result;
import com.young.mapper.SysNotificationMapper;
import com.young.pojo.SysNotification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统通知接口
 */
@Tag(name = "系统通知")
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private SysNotificationMapper notificationMapper;

    @Operation(summary = "获取通知列表")
    @GetMapping
    public Result<List<SysNotification>> getNotifications(
            @RequestParam(defaultValue = "20") Integer limit,
            HttpServletRequest request) {
        Long userId = Long.valueOf(String.valueOf(request.getAttribute("userId")));
        Integer roleId = Integer.valueOf(String.valueOf(request.getAttribute("roleId")));
        // userType: 1=内部用户, 2=客户
        Integer userType = roleId == 3 ? 2 : 1;
        List<SysNotification> notifications = notificationMapper.selectByUserId(userId, userType, limit);
        return Result.success(notifications);
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> getUnreadCount(HttpServletRequest request) {
        Long userId = Long.valueOf(String.valueOf(request.getAttribute("userId")));
        Integer roleId = Integer.valueOf(String.valueOf(request.getAttribute("roleId")));
        Integer userType = roleId == 3 ? 2 : 1;
        int count = notificationMapper.countUnread(userId, userType);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }

    @Operation(summary = "标记单条通知为已读")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationMapper.markAsRead(id);
        return Result.success();
    }

    @Operation(summary = "标记所有通知为已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = Long.valueOf(String.valueOf(request.getAttribute("userId")));
        Integer roleId = Integer.valueOf(String.valueOf(request.getAttribute("roleId")));
        Integer userType = roleId == 3 ? 2 : 1;
        notificationMapper.markAllAsRead(userId, userType);
        return Result.success();
    }
}