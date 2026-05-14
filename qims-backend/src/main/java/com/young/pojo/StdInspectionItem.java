package com.young.pojo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 标准对应的检验项目及限值表
 */
@Data
public class StdInspectionItem {
    /**
     * 项目ID
     */
    private Long id;
    /**
     * 关联的标准ID (外键 std_standard.id)
     */
    private Long standardId;
    /**
     * 检验项目名称 (如: 甲醛含量、pH值、抗拉强度)
     */
    private String itemName;
    /**
     * 计量单位 (如: mg/kg, %, MPa)
     */
    private String unit;
    /**
     * 判定方式：1-数值范围，2-上限值，3-下限值，4-文本定性
     */
    private Integer judgeType;
    /**
     * 下限值 (包含)
     */
    private BigDecimal minValue;
    /**
     * 上限值 (包含)
     */
    private BigDecimal maxValue;
    /**
     * 文本标准描述 (用于定性，如：无异味，表面光滑)
     */
    private String textStandard;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
