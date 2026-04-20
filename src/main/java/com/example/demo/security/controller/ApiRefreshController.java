package com.example.demo.security.controller;

import com.example.demo.security.jwtutil.CustomJWTException;
import com.example.demo.security.jwtutil.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.demo.security.redis.RedisService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ApiRefreshController {
    private final JWTUtil jwtUtil;
    private final RedisService redisService;

    public String getRefreshTokenFromCookie(HttpServletRequest request){
        if (request.getCookies() == null) return null;

        for (Cookie cookie: request.getCookies()){
            if ("refreshToken".equals(cookie.getName())){
                return cookie.getValue();
            }
        }

        return null;
    }

    //토큰 유효기간 검사/재발급
    @PostMapping("/jwt/token/refresh")
    public ResponseEntity<?> getRefreshToken(@RequestHeader("Authorization") String authorization,
                                             HttpServletRequest request, HttpServletResponse response){
        String refreshToken=getRefreshTokenFromCookie(request);

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomJWTException("NULL_REFRESH");
        }

        String accessToken=authorization.substring(7);

        if (authorization != null && authorization.startsWith("Bearer ")) {
            accessToken = authorization.substring(7);
        }

        // accessToken이 있고 아직 안 만료됐으면 그대로 반환
        if (accessToken != null && !checkExpiredToken(accessToken)) {
            return ResponseEntity.ok(Map.of("accessToken", accessToken));
        }

        Map<String, Object> claims=jwtUtil.validateToken(refreshToken);
        Integer userId = (Integer) claims.get("userId");

        // 🔥 2. Redis 검증
        String savedToken = redisService.get(userId);
        System.out.println("saved====>"+savedToken);
        System.out.println("refresh==>"+refreshToken);

        if (savedToken != null && !savedToken.equals(refreshToken)) {
            System.out.println("두번째 if==>"+savedToken);
            throw new CustomJWTException("INVALID_REFRESH");
        }

        String newAccessToken=jwtUtil.generateToken(claims, 1);

        if (checkTime((Long)claims.get("exp"))){
            String newRefreshToken=jwtUtil.generateToken(claims, 120);
            System.out.println("redis=========>"+newRefreshToken);

            Cookie refreshCookie=new Cookie("refreshToken", newRefreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(false);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(60 * 60 * 2);

            response.addCookie(refreshCookie);

            redisService.save(userId, newRefreshToken, 120);
        }

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    //리프레쉬 토큰 유효기간이 1시간 미만으로 남았는지 검사
    private boolean checkTime(Long exp){
        //JWT exp를 날짜로 변환
        Date expDate=new Date((long) exp * (1000));
        //현재 시간과의 차이 계산 - 밀리세컨즈
        long gap=expDate.getTime() - System.currentTimeMillis();
        //분 단위 계산
        long leftMin=gap/(1000*60);
        //1시간 남았는지
        return leftMin < 60;
    }

    //어세스 토큰 유효기간이 남았는지 검사(안 남았으면 true, 남았으면 false)
    private boolean checkExpiredToken(String token){
        try{
            jwtUtil.validateToken(token);
        }catch (CustomJWTException ex){
            System.out.println("checkExpiredToken ==> " + ex.getMessage());
            if (ex.getMessage().equals("Expired")){
                return true;
            }
            throw ex;
        }
        return false;
    }
}
