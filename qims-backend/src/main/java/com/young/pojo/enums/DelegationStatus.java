package com.young.pojo.enums;

import lombok.Getter;

@Getter
public enum DelegationStatus {
    WAIT_SAMPLE(0, "待收样"),
    IN_PROGRESS(1, "检测中"),
    UNDER_REVIEW(2, "审核中"),
    REPORT_ISSUED(3, "已出报告");

    private final int code;
    private final String description;

    DelegationStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static DelegationStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (DelegationStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
