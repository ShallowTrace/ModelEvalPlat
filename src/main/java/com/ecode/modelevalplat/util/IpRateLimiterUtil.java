package com.ecode.modelevalplat.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class IpRateLimiterUtil {

    private final StringRedisTemplate redisTemplate;

    public boolean isRateLimited(String keyPrefix, String clientIp, int maxPerHour) {
        String hour = String.valueOf(LocalDateTime.now().getHour());
        String redisKey = keyPrefix + ":" + clientIp + ":" + hour;

        String script =
                "local current = redis.call('GET', KEYS[1])\n" +
                        "if current == false then\n" +
                        "   redis.call('SET', KEYS[1], 1)\n" +
                        "   redis.call('EXPIRE', KEYS[1], 3600)\n" +
                        "   return 0\n" +
                        "else\n" +
                        "   if tonumber(current) >= tonumber(ARGV[1]) then\n" +
                        "       return 1\n" +
                        "   else\n" +
                        "       redis.call('INCR', KEYS[1])\n" +
                        "       return 0\n" +
                        "   end\n" +
                        "end";

        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class),
                Collections.singletonList(redisKey),
                String.valueOf(maxPerHour)
        );

        return result != null && result == 1;
    }
}
