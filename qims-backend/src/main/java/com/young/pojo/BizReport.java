package com.young.pojo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 最终检验报告主表
 */
@Data
public class BizReport {
    /**
     * 报告ID
     */
    private Long id;
    /**
     * 报告编号 (防伪查询用，如: R202310240001)
     */
    private String reportNo;
    /**
     * 关联的委托单ID
     */
    private Long delegationId;
    /**
     * 审核/签发人ID
     */
    private Long reviewerId;
    /**
     * 最终综合判定：1-合格，0-不合格
     */
    private Integer finalConclusion;
    /**
     * 生成的PDF报告文件存储路径
     */
    private String reportFileUrl;
    /**
     * 签发日期
     */
    private LocalDateTime issueTime;
}
