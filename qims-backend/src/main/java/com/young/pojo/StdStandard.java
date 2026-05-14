package com.young.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StdStandard {
    /**
     * 标准ID
     */
    private Long id;
    /**
     * 标准代号
     */
    private String standardCode;
    /**
     * 标准名称
     */
    private String standardName;
    private String standardCategory;
    /**
     * 适用食品类型
     */
    private String productCategory;
    /**
     * 状态：1现行，0废止
     */
    private Integer status;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
