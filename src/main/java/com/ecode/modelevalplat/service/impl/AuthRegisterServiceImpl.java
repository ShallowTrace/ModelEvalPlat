package com.ecode.modelevalplat.service.impl;

import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.common.StatusEnum;
import com.ecode.modelevalplat.dao.entity.UserDO;
import com.ecode.modelevalplat.dao.mapper.AuthRegisterMapper;
import com.ecode.modelevalplat.dto.AuthRegisterRequestDTO;
import com.ecode.modelevalplat.kafka.dto.MailMessageDTO;
import com.ecode.modelevalplat.kafka.producer.MailProducer;
import com.ecode.modelevalplat.service.AuthRegisterService;
import com.ecode.modelevalplat.util.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

// 注册服务实现类，包含核心业务逻辑
/**
 * 注册核心业务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthRegisterServiceImpl implements AuthRegisterService {

    private final AuthRegisterMapper authRegisterMapper;
    private final StringRedisTemplate redisTemplate;
    private final MailProducer mailProducer;
    private final RedisDistributedLock redisDistributedLock;
    private final MetricsUtil metricsUtil;
    private final IpRateLimiterUtil ipRateLimiterUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    // 验证码 Redis key 前缀
    private static final String REDIS_VERIFY_CODE_PREFIX = "verify_code:";

    // 验证码发送次数限制 key 前缀
    private static final String REDIS_VERIFY_CODE_DAY_LIMIT_PREFIX = "verify_code_day_limit:";

    // 分布式锁 key 前缀
    private static final String DISTRIBUTED_LOCK_PREFIX = "lock:";

    // 验证码有效期 15分钟
    private static final Duration VERIFY_CODE_EXPIRE = Duration.ofMinutes(15);

    // 验证码发送最短间隔 2分钟
    private static final Duration VERIFY_CODE_SEND_INTERVAL = Duration.ofMinutes(2);

    // 每日最多发送次数
    private static final int MAX_VERIFY_CODE_PER_DAY = 10;

    // 分布式锁超时时间
    private static final long LOCK_EXPIRE_TIME = 10L;

    // IP 发送次数限制 key 前缀
    private static final int MAX_VERIFY_CODE_PER_IP_PER_HOUR = 500;

    // IP 访问限制 key 前缀
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:register_ip";

    /**
     * 发送邮箱验证码业务
     */
    @Override
    @CircuitBreaker(name = "sendVerifyCode", fallbackMethod = "sendVerifyCodeFallback")
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.REPEATABLE_READ)
    public ResVo<String> sendVerifyCode(HttpServletRequest request, String username, String email, String password, String confirmPassword) {
        long startTime = System.currentTimeMillis();
        String operation = "sendVerifyCode";

        try {
            // 1. 参数校验（用户名、邮箱、密码格式、两次密码一致）
            if (!UsernameUtils.isValid(username)) {
                metricsUtil.incrementCounter("register.validation.failed", "reason", "username_invalid");
                return ResVo.fail(StatusEnum.USERNAME_WEAK);
            }
            if (!EmailUtils.isValid(email)) {
                metricsUtil.incrementCounter("register.validation.failed", "reason", "email_invalid");
                return ResVo.fail(StatusEnum.INVALID_EMAIL_FORMAT, email);
            }
            if (!PasswordUtils.isStrong(password)) {
                metricsUtil.incrementCounter("register.validation.failed", "reason", "password_invalid");
                return ResVo.fail(StatusEnum.PASSWORD_WEAK);
            }
            if (!password.equals(confirmPassword)) {
                metricsUtil.incrementCounter("register.validation.failed", "reason", "password_mismatch");
                return ResVo.fail(StatusEnum.PASSWORD_TWICE);
            }

            // 2. 获取分布式锁，防止并发操作
            String lockKey = DISTRIBUTED_LOCK_PREFIX + "send_code:" + email;
            String lockValue = redisDistributedLock.acquireLock(lockKey, LOCK_EXPIRE_TIME);
            if (lockValue == null) {
                metricsUtil.incrementCounter("register.lock.failed");
                return ResVo.fail(StatusEnum.SYSTEM_BUSY);
            }

            try {
                // 3. 判断用户是否已存在（用户名或邮箱）
                if (authRegisterMapper.existsByUsername(username)) {
                    metricsUtil.incrementCounter("register.user.exists", "type", "username");
                    return ResVo.fail(StatusEnum.USER_ALREADY_EXISTS, username);
                }
                if (authRegisterMapper.existsByEmail(email)) {
                    metricsUtil.incrementCounter("register.user.exists", "type", "email");
                    return ResVo.fail(StatusEnum.EMAIL_ALREADY_EXISTS, email);
                }

                String clientIp = IpUtils.getClientIp(request); // 你自己实现，从请求头或上下文中拿

                // 限流逻辑改为调用工具类
                if (ipRateLimiterUtil.isRateLimited(RATE_LIMIT_KEY_PREFIX, clientIp, MAX_VERIFY_CODE_PER_IP_PER_HOUR)) {
                    metricsUtil.incrementCounter("register.rate_limit.ip_exceeded");
                    return ResVo.fail(StatusEnum.IP_RATE_LIMIT_EXCEEDED); // 自定义状态码
                }

                // 4. Redis限流校验：间隔发送
                String sendIntervalKey = REDIS_VERIFY_CODE_PREFIX + email + ":send_interval";
                Boolean hasInterval = redisTemplate.hasKey(sendIntervalKey);
                if (hasInterval) {
                    metricsUtil.incrementCounter("register.rate_limit.exceeded", "type", "interval");
                    return ResVo.fail(StatusEnum.VERIFY_CODE_TOO_FREQUENT);
                }

                // 5. Redis每日发送次数限制
                String dayLimitKey = REDIS_VERIFY_CODE_DAY_LIMIT_PREFIX + email + ":" + LocalDate.now();
                String dayCountStr = redisTemplate.opsForValue().get(dayLimitKey);
                int dayCount = dayCountStr == null ? 0 : Integer.parseInt(dayCountStr);
                if (dayCount >= MAX_VERIFY_CODE_PER_DAY) {
                    metricsUtil.incrementCounter("register.rate_limit.exceeded", "type", "daily");
                    return ResVo.fail(StatusEnum.VERIFY_CODE_TOO_FREQUENT);
                }

                // 6. 生成验证码（6位数字）
                String code = VerifyCodeUtils.generate6DigitCode();

                // 7. 保存验证码到 Redis (有效期15分钟)
                String verifyCodeKey = REDIS_VERIFY_CODE_PREFIX + email + ":code";
                redisTemplate.opsForValue().set(verifyCodeKey, code, VERIFY_CODE_EXPIRE);

                // 8. 设置发送间隔标记 (过期时间2分钟)
                redisTemplate.opsForValue().set(sendIntervalKey, "1", VERIFY_CODE_SEND_INTERVAL);

                // 9. 计数+1 设置过期时间为当天结束（秒数）
                if (dayCount == 0) {
                    long secondsUntilEndOfDay = Duration.between(LocalDateTime.now(), LocalDate.now().plusDays(1).atStartOfDay()).getSeconds();
                    redisTemplate.opsForValue().set(dayLimitKey, "1", secondsUntilEndOfDay, TimeUnit.SECONDS);
                } else {
                    redisTemplate.opsForValue().increment(dayLimitKey);
                }

                // 10. 创建未激活用户
                String hashedPassword = passwordEncoder.encode(password);
                UserDO user = new UserDO();
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(hashedPassword);
                user.setRole("user");
                user.setIsActive(false);// 设置为未激活状态
                user.setCreatedAt(LocalDateTime.now());

                try {
                    authRegisterMapper.insert(user);
                } catch (DuplicateKeyException e) {
                    metricsUtil.incrementCounter("register.db.duplicate");
                    log.warn("用户已存在，email: {}, username: {}", email, username);
                    return ResVo.fail(StatusEnum.USER_ALREADY_EXISTS, username);
                }

                // 11. 构建邮件消息，发送 Kafka 异步发送邮件
                MailMessageDTO mailMessage = new MailMessageDTO();
                mailMessage.setTo(email);
                mailMessage.setSubject("【中国工商银行算法测评平台】注册验证码");
                mailMessage.setContent("您的验证码是：" + code + "，有效期15分钟，请勿泄露。");

                mailProducer.sendMail(mailMessage);

                log.info("验证码已发送到邮箱: {}", email);
                metricsUtil.incrementCounter("register.send_code.success");
                metricsUtil.recordSuccess(operation, System.currentTimeMillis() - startTime);
                return ResVo.ok(StatusEnum.VERIFY_CODE_SENT);

            } finally {
                // 释放分布式锁
                redisDistributedLock.releaseLock(lockKey, lockValue);
            }

        } catch (Exception e) {
            log.error("发送验证码失败，email: {}", email, e);
            metricsUtil.incrementCounter("register.send_code.failed", "exception", e.getClass().getSimpleName());
            metricsUtil.recordFailure(operation, System.currentTimeMillis() - startTime);
            return ResVo.fail(StatusEnum.SYSTEM_ABNORMALITY);
        }
    }

    /**
     * 发送验证码降级处理
     */
    public ResVo<String> sendVerifyCodeFallback(String username, String email, String password,
                                                String confirmPassword, Throwable throwable) {
        log.error("发送验证码服务降级，email: {}", email, throwable);
        metricsUtil.incrementCounter("register.send_code.fallback");
        return ResVo.fail(StatusEnum.SYSTEM_BUSY);
    }

    /**
     * 注册业务实现，幂等，必须携带验证码
     */
    @Override
    @CircuitBreaker(name = "register", fallbackMethod = "registerFallback")
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.REPEATABLE_READ)
    public ResVo<String> register(AuthRegisterRequestDTO dto) {
        long startTime = System.currentTimeMillis();
        String operation = "register";

        try {
            String username = dto.getUsername();
            String email = dto.getEmail();
            String verifyCode = dto.getVerifyCode();

            // 1. 基础参数校验
            if (!UsernameUtils.isValid(username)) {
                metricsUtil.incrementCounter("register.validation.failed", "reason", "username_invalid");
                return ResVo.fail(StatusEnum.USERNAME_WEAK);
            }
            if (!EmailUtils.isValid(email)) {
                metricsUtil.incrementCounter("register.validation.failed", "reason", "email_invalid");
                return ResVo.fail(StatusEnum.INVALID_EMAIL_FORMAT, email);
            }

            // 2. 获取分布式锁，防止并发操作
            String lockKey = DISTRIBUTED_LOCK_PREFIX + "register:" + email;
            String lockValue = redisDistributedLock.acquireLock(lockKey, LOCK_EXPIRE_TIME);
            if (lockValue == null) {
                metricsUtil.incrementCounter("register.lock.failed");
                return ResVo.fail(StatusEnum.SYSTEM_BUSY);
            }

            try {
                // 3. 校验验证码存在与正确性
                String verifyCodeKey = REDIS_VERIFY_CODE_PREFIX + email + ":code";
                String cachedCode = redisTemplate.opsForValue().get(verifyCodeKey);
                if (cachedCode == null) {
                    metricsUtil.incrementCounter("register.verify_code.expired");
                    return ResVo.fail(StatusEnum.VERIFY_CODE_EXPIRED);
                }
                if (!cachedCode.equalsIgnoreCase(verifyCode)) {
                    metricsUtil.incrementCounter("register.verify_code.incorrect");
                    return ResVo.fail(StatusEnum.VERIFY_CODE_INCORRECT);
                }

                // 4. 查找未激活用户并激活
                UserDO user = authRegisterMapper.getUnactivatedUserByEmail(email);
                if (user == null) {
                    metricsUtil.incrementCounter("register.user.not_found");
                    return ResVo.fail(StatusEnum.VERIFY_CODE_EXPIRED);
                }

                // 5. 激活用户
                int updateCount = authRegisterMapper.activateUserByEmail(email);
                if (updateCount != 1) {
                    metricsUtil.incrementCounter("register.user.already_activated");
                    return ResVo.fail(StatusEnum.USER_ALREADY_EXISTS);
                }

                // 6. 删除 Redis 验证码，避免重复使用
                redisTemplate.delete(verifyCodeKey);

                log.info("用户激活成功，用户名：{}", username);
                metricsUtil.incrementCounter("register.success");
                metricsUtil.recordSuccess(operation, System.currentTimeMillis() - startTime);
                return ResVo.ok(StatusEnum.REGISTER_SUCCESS);

            } finally {
                // 释放分布式锁
                redisDistributedLock.releaseLock(lockKey, lockValue);
            }

        } catch (Exception e) {
            log.error("用户注册失败，username: {}", dto.getUsername(), e);
            metricsUtil.incrementCounter("register.failed", "exception", e.getClass().getSimpleName());
            metricsUtil.recordFailure(operation, System.currentTimeMillis() - startTime);
            return ResVo.fail(StatusEnum.SYSTEM_ABNORMALITY);
        }
    }

    /**
     * 注册降级处理
     */
    public ResVo<String> registerFallback(AuthRegisterRequestDTO dto, Throwable throwable) {
        log.error("注册服务降级，username: {}", dto.getUsername(), throwable);
        metricsUtil.incrementCounter("register.fallback");
        return ResVo.fail(StatusEnum.SYSTEM_BUSY);
    }
}