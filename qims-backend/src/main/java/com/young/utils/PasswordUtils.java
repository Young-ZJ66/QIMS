package com.young.utils;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 密码加密工具类（BCrypt）
 * <p>
 * BCrypt 自带随机盐值，单次哈希成本因子默认为 10（约 100ms）。
 * </p>
 */
public class PasswordUtils {

    private static final Logger log = LoggerFactory.getLogger(PasswordUtils.class);

    /**
     * 使用 BCrypt 加密明文密码
     *
     * @param plainPassword 明文密码
     * @return BCrypt 哈希字符串
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return null;
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * 验证密码是否匹配
     *
     * @param plainPassword 用户输入的明文密码
     * @param storedHash    数据库中存储的 BCrypt 哈希
     * @return true 如果密码匹配
     */
    public static boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || plainPassword.isEmpty() || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (Exception e) {
            log.error("BCrypt 密码验证异常", e);
            return false;
        }
    }
}
