package com.example.demo.security.controller;

import com.example.demo.security.jwtutil.CustomJWTException;
import com.example.demo.security.jwtutil.JWTUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ApiRefreshController {

    private final JWTUtil jwtUtil;

    @PostMapping("/jwt/token/refresh")
    public ResponseEntity<?> getRefreshToken(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomJWTException("NULL_REFRESH");
        }

        String accessToken = null;

        if (authorization != null && authorization.startsWith("Bearer ")) {
            accessToken = authorization.substring(7);
        }

        // accessToken이 있고 아직 안 만료됐으면 그대로 반환
        if (accessToken != null && !checkExpiredToken(accessToken)) {
            return ResponseEntity.ok(Map.of("accessToken", accessToken));
        }

        Map<String, Object> claims = jwtUtil.validateToken(refreshToken);

        String newAccessToken = jwtUtil.generateToken(claims, 1); // 테스트용 1분
        String newRefreshToken = refreshToken;

        if (checkTime((Long) claims.get("exp"))) {
            newRefreshToken = jwtUtil.generateToken(claims, 60 * 2);

            ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken)
                    .httpOnly(true)
                    .secure(false) // 운영 HTTPS면 true
                    .path("/")
                    .sameSite("Lax") // cross-origin 쿠키면 None 검토
                    .maxAge(Duration.ofDays(14))
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    // refresh token 유효기간이 1시간 미만으로 남았는지 검사
    private boolean checkTime(Long exp) {
        Date expDate = new Date(exp * 1000);
        long gap = expDate.getTime() - System.currentTimeMillis();
        long leftMin = gap / (1000 * 60);
        return leftMin < 60;
    }

    // access token 유효기간이 남았는지 검사(안 남았으면 true, 남았으면 false)
    private boolean checkExpiredToken(String token) {
        try {
            jwtUtil.validateToken(token);
        } catch (CustomJWTException ex) {
            System.out.println("checkExpiredToken ==> " + ex.getMessage());
            if ("Expired".equals(ex.getMessage())) {
                return true;
            }
            throw ex;
        }
        return false;
    }
}