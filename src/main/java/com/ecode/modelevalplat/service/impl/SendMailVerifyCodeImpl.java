package com.ecode.modelevalplat.service.impl;


import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.common.enums.StatusEnum;
import com.ecode.modelevalplat.kafka.dto.MailMessageDTO;
import com.ecode.modelevalplat.kafka.producer.MailProducer;
import com.ecode.modelevalplat.service.SendMailVerifyCodeService;
import com.ecode.modelevalplat.util.EmailUtils;
import com.ecode.modelevalplat.util.RedisDistributedLock;
import com.ecode.modelevalplat.util.VerifyCodeUtils;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendMailVerifyCodeImpl implements SendMailVerifyCodeService {
    private final RedisTemplate<String, String> redisTemplate;

    private static final long VERIFY_CODE_EXPIRE_MINUTES = 15;
    private static final long VERIFY_CODE_SEND_INTERVAL_SECONDS = 120;
    private static final int MAX_VERIFY_CODE_PER_DAY = 10;
    private final RedisDistributedLock redisDistributedLock;
    private final MailProducer mailProducer;

    @Override
    @CircuitBreaker(name = "sendEmail", fallbackMethod = "sendEmailVerifyCodeFallback")
    public ResVo<String> sendEmailVerifyCode(String email) {
        // 校验邮箱格式
        if (!EmailUtils.isValid(email)) {
            return ResVo.fail(StatusEnum.LOGIN_INVALID_EMAIL_FORMAT);
        }

        // 分布式锁控制发送频率
        String lockKey = "send_code_lock:" + email;
        String lockValue = redisDistributedLock.acquireLock(lockKey, VERIFY_CODE_SEND_INTERVAL_SECONDS);
        if (lockValue == null) {
            return ResVo.fail(StatusEnum.EMAIL_CODE_SEND_TOO_FREQUENT);
        }

        try {
            // 检查当日发送次数
            String dailyCountKey = "verify_code_daily_count:" + email;
            String countStr = redisTemplate.opsForValue().get(dailyCountKey);
            int count = countStr == null ? 0 : Integer.parseInt(countStr);
            if (count >= MAX_VERIFY_CODE_PER_DAY) {
                return ResVo.fail(StatusEnum.MAX_VERIFY_CODE_REQUESTS_PER_ACCOUNT_PER_DAY);
            }

            // 生成验证码
            String verifyCode = VerifyCodeUtils.generate6DigitCode();
            String redisKey = "verify_code:" + email;
            redisTemplate.opsForValue().set(redisKey, verifyCode, VERIFY_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            redisTemplate.opsForValue().increment(dailyCountKey, 1);
            redisTemplate.expire(dailyCountKey, 1, TimeUnit.DAYS);

            // 发送邮件
            try {
                MailMessageDTO mailMessage = new MailMessageDTO();
                mailMessage.setTo(email);
                mailMessage.setSubject("中国工商银行AI模型测评平台登录验证码");
                mailMessage.setContent("您的登录验证码是：" + verifyCode + "，有效期15分钟。");
                mailProducer.sendMail(mailMessage);
                return ResVo.ok(StatusEnum.EMAIL_CODE_SENT_SUCCESS);
            } catch (Exception e) {
                // 可选：记录异常日志
                // log.error("发送邮箱验证码失败", e);
                return ResVo.fail(StatusEnum.SYSTEM_BUSY);
            }

        } finally {
            // 释放锁（防止异常导致锁一直持有）
            if (!redisDistributedLock.releaseLock(lockKey, lockValue)) {
                log.warn("释放验证码发送锁失败，lockKey: {}", lockKey);
            }
        }
    }

    public ResVo<String> sendEmailVerifyCodeFallback(String email, Throwable t) {
        log.error("发送邮箱验证码失败，熔断触发，email: {}, 原因: {}", email, t.getMessage());
        return ResVo.fail(StatusEnum.SYSTEM_ABNORMALITY);
    }
}
