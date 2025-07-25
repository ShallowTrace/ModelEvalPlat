package com.ecode.modelevalplat.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDistributedLock {

    private final StringRedisTemplate redisTemplate;

    private static final String LOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "   return redis.call('del', KEYS[1]) " +
                    "else " +
                    "   return 0 " +
                    "end";

    private static final String ACQUIRE_SCRIPT =
            "if redis.call('exists', KEYS[1]) == 0 then " +
                    "   return redis.call('set', KEYS[1], ARGV[1], 'EX', ARGV[2], 'NX') " +
                    "else " +
                    "   return nil " +
                    "end";

    /**
     * 获取分布式锁
     * @param lockKey 锁的key
     * @param expireTime 过期时间（秒）
     * @return 锁的标识，用于释放锁
     */
    public String acquireLock(String lockKey, long expireTime) {
        String lockValue = UUID.randomUUID().toString();
        try {
            DefaultRedisScript<String> script = new DefaultRedisScript<>();
            script.setScriptText(ACQUIRE_SCRIPT);
            script.setResultType(String.class);

            String result = redisTemplate.execute(
                    script,
                    Collections.singletonList(lockKey),
                    lockValue,
                    String.valueOf(expireTime)
            );

            return result != null ? lockValue : null;
        } catch (Exception e) {
            log.error("获取分布式锁失败，lockKey: {}", lockKey, e);
            return null;
        }
    }

    /**
     * 释放分布式锁
     * @param lockKey 锁的key
     * @param lockValue 锁的值
     * @return 是否释放成功
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(LOCK_SCRIPT);
            script.setResultType(Long.class);

            Long result = redisTemplate.execute(
                    script,
                    Collections.singletonList(lockKey),
                    lockValue
            );

            return result != null && result > 0;
        } catch (Exception e) {
            log.error("释放分布式锁失败，lockKey: {}", lockKey, e);
            return false;
        }
    }
}