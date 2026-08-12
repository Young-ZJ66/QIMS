package com.young.utils;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录限流组件（内存实现）
 * <p>
 * 同一账号连续失败超过阈值后锁定一段时间，防止暴力破解。
 * </p>
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MILLIS = 5 * 60 * 1000;

    private static final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private static final Map<String, Long> lockUntil = new ConcurrentHashMap<>();

    /**
     * 检查账号是否被锁定
     *
     * @param username 登录账号
     * @return true 如果被锁定
     */
    public boolean isLocked(String username) {
        Long lockTime = lockUntil.get(username);
        if (lockTime == null) {
            return false;
        }
        if (System.currentTimeMillis() < lockTime) {
            return true;
        }
        // 锁定已过期，清理记录
        lockUntil.remove(username);
        failedAttempts.remove(username);
        return false;
    }

    /**
     * 获取剩余锁定时间（秒）
     */
    public long getRemainingLockSeconds(String username) {
        Long lockTime = lockUntil.get(username);
        if (lockTime == null) {
            return 0;
        }
        long remaining = lockTime - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000 : 0;
    }

    /**
     * 记录一次失败尝试
     */
    public void recordFailure(String username) {
        int attempts = failedAttempts.getOrDefault(username, 0) + 1;
        failedAttempts.put(username, attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            lockUntil.put(username, System.currentTimeMillis() + LOCK_DURATION_MILLIS);
        }
    }

    /**
     * 登录成功时重置计数
     */
    public void recordSuccess(String username) {
        failedAttempts.remove(username);
        lockUntil.remove(username);
    }
}
