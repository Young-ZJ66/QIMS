package com.young.pojo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 送检客户/企业表
 */
@Data
public class SysClient {
    /**
     * 客户ID
     */
    private Long id;
    /**
     * 企业名称
     */
    private String companyName;
    /**
     * 联系人
     */
    private String contactPerson;
    /**
     * 联系电话
     */
    private String phone;
    /**
     * 企业地址
     */
    private String address;
    /**
     * 客户登录账号
     */
    private String loginAccount;
    /**
     * 登录密码
     */
    private String loginPassword;
    /**
     * 注册时间
     */
    private LocalDateTime createTime;
}
