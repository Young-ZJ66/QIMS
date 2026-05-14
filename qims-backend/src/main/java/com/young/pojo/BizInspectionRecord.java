package com.young.pojo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 实验室单项检测数据记录表
 */
@Data
public class BizInspectionRecord {
    /**
     * 记录ID
     */
    private Long id;
    /**
     * 关联的任务ID (外键 biz_sample_task.id)
     */
    private Long taskId;
    /**
     * 检测项目ID (外键 std_inspection_item.id)
     */
    private Long itemId;
    /**
     * 实测数值
     */
    private BigDecimal measuredValue;
    /**
     * 实测文本描述
     */
    private String measuredText;
    private String photoUrl;
    /**
     * 单项结论：1-合格，0-不合格 (系统根据录入值与标准对比自动得出)
     */
    private Integer result;
    /**
     * 附件图片地址 (如仪器屏幕截图)
     */
    private String attachmentUrl;
    /**
     * 检测时间
     */
    private LocalDateTime inspectTime;
    private String itemName;
    private String blindSampleCode;
}
