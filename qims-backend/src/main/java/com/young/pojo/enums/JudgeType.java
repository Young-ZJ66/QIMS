package com.young.pojo.enums;

import lombok.Getter;

@Getter
public enum JudgeType {
    RANGE(1, "数值范围"),
    UPPER_LIMIT(2, "上限值"),
    LOWER_LIMIT(3, "下限值"),
    QUALITATIVE(4, "文本定性");

    private final int code;
    private final String description;

    JudgeType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static JudgeType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (JudgeType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
