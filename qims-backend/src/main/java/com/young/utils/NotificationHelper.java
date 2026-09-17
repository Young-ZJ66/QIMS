package com.young.utils;

import com.young.mapper.SysNotificationMapper;
import com.young.pojo.SysNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 通知工具类 - 用于在业务流程中发送系统通知
 */
@Component
public class NotificationHelper {

    @Autowired
    private SysNotificationMapper notificationMapper;

    /**
     * 发送通知给内部用户（管理员/检测员）
     */
    public void notifyInternalUser(Long userId, String title, String content, String type, String bizType, Long bizId) {
        SysNotification notification = new SysNotification();
        notification.setUserId(userId);
        notification.setUserType(1);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setBizType(bizType);
        notification.setBizId(bizId);
        notificationMapper.insert(notification);
    }

    /**
     * 发送通知给客户
     */
    public void notifyClient(Long clientId, String title, String content, String type, String bizType, Long bizId) {
        SysNotification notification = new SysNotification();
        notification.setUserId(clientId);
        notification.setUserType(2);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setBizType(bizType);
        notification.setBizId(bizId);
        notificationMapper.insert(notification);
    }
}