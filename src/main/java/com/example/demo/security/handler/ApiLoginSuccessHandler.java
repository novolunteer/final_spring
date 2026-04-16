package com.example.demo.security.handler;

import com.example.demo.security.jwtutil.JWTUtil;
import com.example.demo.security.redis.RedisService;
import com.example.demo.security.security.CustomUserDetails;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ApiLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final RedisService redisService;
    private final JWTUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();

        Map<String, Object> claims = details.getClaims();

        String accessToken = jwtUtil.generateToken(claims, 1);
        String refreshToken = jwtUtil.generateToken(claims, 2);

        // accessToken만 body에 포함
        claims.put("accessToken", accessToken);
        redisService.save(details.getUserId(), refreshToken, 2);
        // refreshToken은 HttpOnly 쿠키로 저장
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)   // 운영 환경 HTTPS면 true
                .path("/")
                .sameSite("Lax") // 크로스 사이트 이슈 있으면 None 검토
                .maxAge(Duration.ofHours(2))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        Gson gson = new Gson();
        String jsonStr = gson.toJson(claims);

        response.setContentType("application/json;charset=utf-8");
        PrintWriter pw = response.getWriter();
        pw.println(jsonStr);
        pw.close();
    }
}