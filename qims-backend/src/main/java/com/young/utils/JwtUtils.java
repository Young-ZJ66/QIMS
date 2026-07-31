package com.young.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类（Spring Bean，密钥从配置文件读取，保证服务重启后 Token 仍然有效）
 */
@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    /** 从配置文件读取的密钥 */
    @Value("${qims.jwt.secret}")
    private String secretKey;

    /** Token 有效期（小时），从配置文件读取 */
    @Value("${qims.jwt.expire-hours:24}")
    private int expireHours;

    /** 实际用于签名的 Key 对象，在初始化时生成 */
    private Key key;

    /** Token 有效期（毫秒） */
    private long expireTimeMillis;

    @PostConstruct
    public void init() {
        // 确保密钥长度满足 HS256 的最低要求（256 bit = 32 byte）
        byte[] keyBytes = secretKey.getBytes();
        if (keyBytes.length < 32) {
            // 密钥过短时进行填充，保证安全性
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            key = Keys.hmacShaKeyFor(paddedKey);
            log.warn("JWT 密钥长度不足32字节，已自动填充。生产环境请配置更长的密钥。");
        } else {
            key = Keys.hmacShaKeyFor(keyBytes);
        }
        expireTimeMillis = expireHours * 60L * 60L * 1000L;
        log.info("JWT 工具类初始化完成，Token 有效期: {} 小时", expireHours);
    }

    /**
     * 生成 Token
     */
    public String generateToken(Long userId, String username, Integer roleId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roleId", roleId);
        return generateToken(claims);
    }

    /**
     * 生成 Token（携带额外 claims，如 clientId）
     */
    public String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expireTimeMillis))
                .signWith(key)
                .compact();
    }

    /**
     * 解析 Token
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 判断 Token 是否即将过期（剩余时间小于有效期的 1/4），用于静默刷新
     *
     * @param token JWT Token
     * @return true 如果 Token 即将过期
     */
    public boolean isTokenExpiringSoon(String token) {
        try {
            Claims claims = parseToken(token);
            long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
            return remaining < (expireTimeMillis / 4);
        } catch (Exception e) {
            return true;
        }
    }
}
