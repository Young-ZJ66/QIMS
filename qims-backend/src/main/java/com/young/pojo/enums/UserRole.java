package com.young.pojo.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN(1, "管理员"),
    INSPECTOR(2, "检测员");

    private final int code;
    private final String description;

    UserRole(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static UserRole of(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserRole role : values()) {
            if (role.code == code) {
                return role;
            }
        }
        return null;
    }
}
