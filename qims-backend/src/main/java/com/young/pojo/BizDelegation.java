package com.young.pojo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 检验委托单主表
 */
@Data
public class BizDelegation {
    /**
     * 委托单ID
     */
    private Long id;
    /**
     * 委托单号 (如: D202310240001)
     */
    private String delegationNo;
    /**
     * 送检企业ID (外键 sys_client.id)
     */
    private Long clientId;
    /**
     * 样品名称
     */
    private String sampleName;
    /**
     * 规格型号
     */
    private String sampleSpecs;
    /**
     * 送样数量
     */
    private Integer sampleQuantity;
    /**
     * 要求依据的检测标准ID (外键 std_standard.id)
     */
    private Long standardId;
    /**
     * 状态：0-待收样，1-检测中，2-审核中，3-已出报告
     */
    private Integer status;
    /**
     * 委托提交时间
     */
    private LocalDateTime submitTime;

    // 关联展示字段
    private String clientName; // 委托方企业名称
}
