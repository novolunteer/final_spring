package com.example.demo.security.controller;

import com.example.demo.security.jwtutil.CustomJWTException;
import com.example.demo.security.jwtutil.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ApiRefreshController {
    private final JWTUtil jwtUtil;

    //토큰 유효기간 검사/재발급
    @RequestMapping("/api/user/refresh")
    public ResponseEntity<?> getRefreshToken(@RequestHeader("Authorization") String authorization,
                                             @RequestParam("refreshToken") String refreshToken){
        if (refreshToken == null){
            throw new CustomJWTException("NULL_REFRESH");
        }

        if (authorization == null || authorization.length() < 7){
            throw new CustomJWTException("INVALID_REFRESH");
        }

        String accessToken=authorization.substring(7);
        if (!checkExpiredToken(accessToken)){ //유효기간 아직 남음
            return ResponseEntity.ok(Map.of("accessToken", accessToken, "refreshToken", refreshToken));
        }

        Map<String, Object> claims=jwtUtil.validateToken(refreshToken);
        String newAccessToken=jwtUtil.generateToken(claims, 1); //테스트 하려고 1분 설정
        String newRefreshToken=refreshToken;
        if (checkTime((Long)claims.get("exp"))){
            newRefreshToken= jwtUtil.generateToken(claims, 60*2);
        }

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken));
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
        }
        return false;
    }
}
