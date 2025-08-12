package com.ecode.modelevalplat.Security;

import com.ecode.modelevalplat.context.UserContext;
import com.ecode.modelevalplat.context.UserContextHolder;
import com.ecode.modelevalplat.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, RedisTemplate<String, String> redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        // 放行无需认证的路径，比如注册、登录等
        if (path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            System.out.print("path: " + path);
            return;
        }

        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        token = token.substring(7); // 去掉 "Bearer "
        try {
            Claims claims = jwtUtil.parseToken(token);
            String userId = claims.get("userId", String.class);
            String username = claims.getSubject(); // 对应 setSubject(username)
            String role = claims.get("role", String.class);

            String redisToken = redisTemplate.opsForValue().get("sso:token:" + username);
            if (!token.equals(redisToken)) {
                throw new RuntimeException("Token 无效或已被替换");
            }

            // 设置用户上下文
            UserContext userContext = new UserContext();
            userContext.setUserId(userId);
            userContext.setUsername(username);
            userContext.setRole(role);
            UserContextHolder.set(userContext);

            // 构建 Spring Security 上下文认证信息（可加入角色权限）
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"" + e.getMessage() + "\",\"data\":null}");
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 清理 ThreadLocal，防止内存泄漏
            UserContextHolder.clear();
        }
    }

}


