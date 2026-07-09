package com.young.pojo.enums;

import lombok.Getter;

@Getter
public enum InspectionResult {
    UNQUALIFIED(0, "不合格"),
    QUALIFIED(1, "合格");

    private final int code;
    private final String description;

    InspectionResult(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static InspectionResult of(Integer code) {
        if (code == null) {
            return null;
        }
        for (InspectionResult res : values()) {
            if (res.code == code) {
                return res;
            }
        }
        return null;
    }
}
