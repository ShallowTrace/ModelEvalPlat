package com.ecode.modelevalplat.service.impl;

import com.ecode.modelevalplat.common.ResVo;
import com.ecode.modelevalplat.common.enums.StatusEnum;
import com.ecode.modelevalplat.config.JwtProperties;
import com.ecode.modelevalplat.dao.entity.UserDO;
import com.ecode.modelevalplat.dao.mapper.AuthLoginMapper;
import com.ecode.modelevalplat.dto.AuthLoginRequestDTO1;
import com.ecode.modelevalplat.dto.AuthLoginRequestDTO2;
import com.ecode.modelevalplat.dto.CaptchaResponseDTO;
import com.ecode.modelevalplat.dto.JwtResponseDTO;
import com.ecode.modelevalplat.service.AuthLoginService;
import com.ecode.modelevalplat.util.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthLoginServiceImpl implements AuthLoginService {

    private final AuthLoginMapper authLoginMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;
    private final RedisDistributedLock redisDistributedLock;
    private final JwtProperties jwtProperties;



    // 登录失败次数限制
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int MAX_LOGIN_ATTEMPTS_TODAY = 20;
    private static final long LOCK_DURATION_MINUTES = 30;
    private static final long LOCK_TODAY_DURATION_HOURS = 24;

    // 验证码相关限制

    private static final long CAPTCHA_EXPIRE_SECONDS = 300;

    //分布式锁
    private static final long LOCK_EXPIRE_SECONDS = 20;

    // 登陆分布式锁 key 前缀
    private static final String DISTRIBUTED_LOCK_PREFIX = "login:lock:";

    //同IP最大登陆用户
    private static final int MAX_LOGIN_USERS_PER_IP = 1000;


    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.REPEATABLE_READ)
    @CircuitBreaker(name = "loginByEmailAndPassword", fallbackMethod = "loginByEmailAndPasswordFallback")
    public ResVo<JwtResponseDTO> loginByEmailAndPassword(AuthLoginRequestDTO1 authLoginRequestDTO1,
                                                         HttpServletRequest httpRequest) {
        String email = authLoginRequestDTO1.getEmail();
        String password = authLoginRequestDTO1.getPassword();
        String dynamicCode = authLoginRequestDTO1.getDynamicCode();
        String uuid = authLoginRequestDTO1.getUuid();

        try {
            // 1. 校验邮箱格式
            if (!EmailUtils.isValid(email)) {
                return ResVo.fail(StatusEnum.LOGIN_INVALID_EMAIL_FORMAT);
            }

            // 2. 校验图形验证码
            String storedCode = redisTemplate.opsForValue().get("captcha:" + uuid);
            if (storedCode == null || !storedCode.equalsIgnoreCase(dynamicCode)) {
                return ResVo.fail(StatusEnum.DYNAMIC_CODE_INVALID);
            }

            // 3. 获取用户信息
            UserDO user = authLoginMapper.selectByEmail(email);
            if (user == null) {
                return ResVo.fail(StatusEnum.USER_NOT_FOUND);
            }

            String userId = String.valueOf(user.getId());

            // ---------------------------
            // 4. 检查账户是否被锁定
            // ---------------------------
            String shortLockKey = "login:lock:short:" + email; // 30分钟锁定key
            String dailyLockKey = "login:lock:daily:" + email; // 24小时锁定key
            if (redisTemplate.hasKey(dailyLockKey)) {
                return ResVo.fail(StatusEnum.ACCOUNT_LOCKED_FOR_TODAY);
            }
            if (redisTemplate.hasKey(shortLockKey)) {
                return ResVo.fail(StatusEnum.TEMP_LOCKED_TOO_MANY_PASSWORD_FAILURES);
            }

            // ---------------------------
            // 5. 密码校验
            // ---------------------------
            if (!BCrypt.checkpw(password, user.getPassword())) {
                // 记录失败次数
                String failCountKey = "login:fail:count:" + email;
                Long failCount = redisTemplate.opsForValue().increment(failCountKey);
                redisTemplate.expire(failCountKey, 1, TimeUnit.DAYS); // 失败次数记录保留1天

                if (failCount != null) {
                    if (failCount >= MAX_LOGIN_ATTEMPTS_TODAY) {
                        // 一天失败次数超限，锁定24小时
                        redisTemplate.opsForValue().set(dailyLockKey, "1", LOCK_TODAY_DURATION_HOURS, TimeUnit.HOURS);
                        return ResVo.fail(StatusEnum.ACCOUNT_LOCKED_FOR_TODAY);
                    } else if (failCount >= MAX_LOGIN_ATTEMPTS) {
                        // 短期失败次数超限，锁定30分钟
                        redisTemplate.opsForValue().set(shortLockKey, "1", LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
                        return ResVo.fail(StatusEnum.TEMP_LOCKED_TOO_MANY_PASSWORD_FAILURES);
                    }
                }

                return ResVo.fail(StatusEnum.WRONG_PASSWORD);
            }

            // ---------------------------
            // 6. IP限流
            // ---------------------------
            String ipAddress1 = IpUtils.getClientIp(httpRequest);
            String ipLoginKey = "ip:login:" + ipAddress1;
            Long userCount = redisTemplate.opsForSet().size(ipLoginKey);
            if (userCount != null && userCount >= MAX_LOGIN_USERS_PER_IP) {
                return ResVo.fail(StatusEnum.TOO_MANY_USERS_LOGGED_IN_FROM_IP);
            }

            // ---------------------------
            // 7. 分布式锁防止并发登录
            // ---------------------------
            String loginLockKey = DISTRIBUTED_LOCK_PREFIX + email;
            String lockValue = redisDistributedLock.acquireLock(loginLockKey, LOCK_EXPIRE_SECONDS);
            if (lockValue == null) {
                return ResVo.fail(StatusEnum.SYSTEM_BUSY);
            }

            try {
                // 清除失败记录（登录成功）
                redisTemplate.delete("login:fail:count:" + email);
                redisTemplate.delete(shortLockKey);

                // 生成 JWT
                JwtResponseDTO jwtResponse1 = new JwtResponseDTO();
                String sessionToken = UUID.randomUUID().toString().replace("-", "");

                jwtResponse1.setToken(jwtUtil.generateToken(sessionToken));

                // 更新最后登录时间
                authLoginMapper.updateLastLoginTime(Long.valueOf(userId));

                // 单点登录缓存
                String redisKey = "sso:token:" + sessionToken;
                redisTemplate.opsForValue().set(redisKey, userId, jwtProperties.getExpiration(), TimeUnit.SECONDS);

                // 加入IP登录集
                redisTemplate.opsForSet().add(ipLoginKey, userId);
                redisTemplate.expire(ipLoginKey, jwtProperties.getExpiration(), TimeUnit.SECONDS);

                return ResVo.ok(StatusEnum.LOGIN_SUCCESS, jwtResponse1);
            } finally {
                if (!redisDistributedLock.releaseLock(loginLockKey, lockValue)) {
                    log.warn("释放登陆1分布式锁失败，lockKey: {}", loginLockKey);
                }
            }

        } catch (Exception e) {
            log.error("用户登录失败，email: {}", email, e);
            return ResVo.fail(StatusEnum.SYSTEM_ABNORMALITY);
        }
    }

    public ResVo<JwtResponseDTO> loginByEmailAndPasswordFallback(AuthLoginRequestDTO1 authLoginRequestDTO1,
                                                                 HttpServletRequest httpRequest,
                                                                 Throwable t) {
        log.error("loginByEmailAndPassword 接口触发熔断或异常：{}", t.getMessage());
        return ResVo.fail(StatusEnum.SYSTEM_ABNORMALITY);
    }


    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.REPEATABLE_READ)
    @CircuitBreaker(name = "loginByEmailCode", fallbackMethod = "loginByEmailCodeFallback")
    public ResVo<JwtResponseDTO> loginByEmailCode(AuthLoginRequestDTO2 authLoginRequestDTO2, HttpServletRequest httpRequest) {
        String email = authLoginRequestDTO2.getEmail();
        String verifyCode = authLoginRequestDTO2.getVerifyCode();
        String dynamicCode = authLoginRequestDTO2.getDynamicCode();
        String uuid = authLoginRequestDTO2.getUuid();

        try {
            // 校验邮箱格式
            if (!EmailUtils.isValid(email)) {
                return ResVo.fail(StatusEnum.LOGIN_INVALID_EMAIL_FORMAT);
            }

            // 2. 校验图形验证码
            String storedCode = redisTemplate.opsForValue().get("captcha:" + uuid);
            if (storedCode == null || !storedCode.equalsIgnoreCase(dynamicCode)) {
                return ResVo.fail(StatusEnum.DYNAMIC_CODE_INVALID);
            }

            // 校验邮箱验证码
            String storedVerifyCode = redisTemplate.opsForValue().get("verify_code:" + email);
            if (storedVerifyCode == null) {
                return ResVo.fail(StatusEnum.PLEASE_REQUEST_CODE_FIRST);
            }
            if (!verifyCode.equals(storedVerifyCode)) {
                return ResVo.fail(StatusEnum.EMAIL_CODE_INVALID);
            }

            // 3. 获取用户信息
            UserDO user = authLoginMapper.selectByEmail(email);
            if (user == null) {
                return ResVo.fail(StatusEnum.USER_NOT_FOUND);
            }
            String userId = String.valueOf(user.getId());

            String ipAddress2 = IpUtils.getClientIp(httpRequest);
            String ipLoginKey = "ip:login:" + ipAddress2;
            Long userCount = redisTemplate.opsForSet().size(ipLoginKey);
            if (userCount != null && userCount >= MAX_LOGIN_USERS_PER_IP) {
                return ResVo.fail(StatusEnum.TOO_MANY_USERS_LOGGED_IN_FROM_IP);
            }

            String loginLockKey = DISTRIBUTED_LOCK_PREFIX + email;
            String lockValue = redisDistributedLock.acquireLock(loginLockKey, LOCK_EXPIRE_SECONDS);
            if (lockValue == null) {
                return ResVo.fail(StatusEnum.SYSTEM_BUSY);
            }
            try {
                // 生成 JWT
                JwtResponseDTO jwtResponse2 = new JwtResponseDTO();
                String sessionToken = UUID.randomUUID().toString().replace("-", "");

                jwtResponse2.setToken(jwtUtil.generateToken(sessionToken));
//              jwtResponse2.setRefreshToken(jwtUtil.generateRefreshToken(sessionToken));


                // 更新最后登录时间
                authLoginMapper.updateLastLoginTime(Long.valueOf(userId));

                // 单点登录缓存
//                String ssoKey = "sso:token:" + user.getUsername();
//                redisTemplate.opsForValue().set(ssoKey, jwtResponse2.getToken(), jwtProperties.getExpiration(), TimeUnit.SECONDS);

                String redisKey = "sso:token:" + sessionToken;
                redisTemplate.opsForValue().set(redisKey, userId, jwtProperties.getExpiration(), TimeUnit.SECONDS);

                // 加入IP登录集
                redisTemplate.opsForSet().add(ipLoginKey, userId);
                redisTemplate.expire(ipLoginKey, jwtProperties.getExpiration(), TimeUnit.SECONDS);

                return ResVo.ok(StatusEnum.LOGIN_SUCCESS, jwtResponse2);
            } finally {
                if (!redisDistributedLock.releaseLock(loginLockKey, lockValue)) {
                    log.warn("释放登陆2分布式锁失败，lockKey: {}", loginLockKey);
                }
            }
        }
        catch (Exception e) {
            log.error("用户登录失败，email: {}", email, e);
            return ResVo.fail(StatusEnum.SYSTEM_ABNORMALITY);
        }

    }

    public ResVo<JwtResponseDTO> loginByEmailCodeFallback(AuthLoginRequestDTO2 authLoginRequestDTO2,
                                                          HttpServletRequest httpRequest,
                                                          Throwable t) {
        log.error("loginByEmailCode 接口触发熔断或异常：{}", t.getMessage());
        return ResVo.fail(StatusEnum.SYSTEM_ABNORMALITY);
    }

    @Override
    public ResVo<CaptchaResponseDTO> generateCaptcha(String uuid) {
        String code = CaptchaUtil.generateCode();
        // 生成验证码图片Base64
        String imageBase64 = CaptchaUtil.generateImageBase64(code);
        // 保存验证码到Redis，key: captcha:uuid, value: code
        String redisKey = "captcha:" + uuid;
        System.out.println(code);
        System.out.println(uuid);
        redisTemplate.opsForValue().set(redisKey, code, CAPTCHA_EXPIRE_SECONDS, TimeUnit.SECONDS);
        CaptchaResponseDTO responseDTO = new CaptchaResponseDTO();
        responseDTO.setUuid(uuid);
        responseDTO.setImageBase64(imageBase64);
        return ResVo.ok(StatusEnum.DYNAMIC_CODE_GENERATE, responseDTO);
    }
    @Override
    public ResVo<Void> logout(String token) {
        try {
            Claims claims = jwtUtil.parseToken(token);
            String username = claims.getSubject();
            if (username == null) {
                return ResVo.fail(StatusEnum.TOKEN_PARSE_FAILED);
            }
            String ssoKey = "sso:token:" + username;

            // 删除Redis中的token，达到退出效果
            Boolean deleted = redisTemplate.delete(ssoKey);
            if (deleted) {
                return ResVo.ok(StatusEnum.LOGOUT_SUCCESS);
            } else {
                return ResVo.fail(StatusEnum.LOGOUT_FAILED);
            }
        } catch (Exception e) {
            return ResVo.fail(StatusEnum.LOGOUT_EXCEPTION);
        }
    }




}

