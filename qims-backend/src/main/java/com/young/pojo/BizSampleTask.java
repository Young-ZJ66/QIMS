package com.young.pojo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 内部盲样与检测任务分配表
 */
@Data
public class BizSampleTask {
    /**
     * 任务/样品ID
     */
    private Long id;
    /**
     * 关联委托单ID (外键 biz_delegation.id)
     */
    private Long delegationId;
    /**
     * 内部盲样编号 (供检测员看，如: SAM-1024-001)
     */
    private String blindSampleCode;
    /**
     * 分配的检测员ID (外键 sys_user.id)
     */
    private Long inspectorId;
    /**
     * 收样时间
     */
    private LocalDateTime receiveTime;
    /**
     * 收样人ID
     */
    private Long receiverId;
    /**
     * 检测状态：0-待检测，1-已检测
     */
    private Integer status;
    private LocalDateTime finishTime;
}
