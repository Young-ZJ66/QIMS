package com.young.pojo.enums;

import lombok.Getter;

@Getter
public enum TaskStatus {
    PENDING(0, "待检测"),
    COMPLETED(1, "已检测");

    private final int code;
    private final String description;

    TaskStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TaskStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (TaskStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
