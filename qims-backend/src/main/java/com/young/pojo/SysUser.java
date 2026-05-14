package com.young.pojo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统内部用户表
 */
@Data
public class SysUser {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * 登录账号
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 角色ID (1-管理员 2-检测员)
     */
    private Integer roleId;
    /**
     * 联系电话
     */
    private String phone;
    /**
     * 状态：1正常，0禁用
     */
    private Integer status;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
