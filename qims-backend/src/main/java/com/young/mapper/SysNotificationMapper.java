package com.young.mapper;

import com.young.pojo.SysNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SysNotificationMapper {
    int insert(SysNotification record);
    int markAsRead(Long id);
    int markAllAsRead(@Param("userId") Long userId, @Param("userType") Integer userType);
    SysNotification selectById(Long id);
    List<SysNotification> selectByUserId(@Param("userId") Long userId, @Param("userType") Integer userType, @Param("limit") Integer limit);
    int countUnread(@Param("userId") Long userId, @Param("userType") Integer userType);
}