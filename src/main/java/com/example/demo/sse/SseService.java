package com.example.demo.sse;

import com.example.demo.security.jwtutil.CustomJWTException;
import com.example.demo.security.jwtutil.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SseService {
    private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final JWTUtil jWTUtil;

    public SseEmitter subscribe(Integer doctorId) {
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);
        System.out.println("🚨 subscribe 진입");
        System.out.println("현재 emitters: " + emitters.keySet());

        if (emitters.containsKey(doctorId)) {
            emitters.get(doctorId).complete(); // 이전 emitter 닫기
        }

        emitters.put(doctorId, emitter);
        System.out.println("현재 emitters: " + emitters.keySet());

        emitter.onCompletion(() -> emitters.remove(doctorId));
        emitter.onTimeout(() -> emitters.remove(doctorId));
        emitter.onError((e) -> emitters.remove(doctorId));

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return emitter;
    }

    public void sendNewReservation(Integer doctorId, Object data) {
        SseEmitter emitter = emitters.get(doctorId);
        System.out.println("🔥 SSE 전송 시도 doctorId = " + doctorId);

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("newReservation")
                        .data(data));
            } catch (Exception e) {
                System.out.println("❌ 전송 실패");
                emitters.remove(doctorId);
            }
        }else {
            System.out.println("❌ emitter 없음");
        }
    }

    public Map<String, Object> getToken(String accessToken,
                                             String refreshToken){
        if (refreshToken == null){
            throw new CustomJWTException("NULL_REFRESH");
        }

        if (accessToken == null){
            throw new CustomJWTException("INVALID_REFRESH");
        }

        if (!checkExpiredToken(accessToken)){ //유효기간 아직 남음
            return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
        }

        Map<String, Object> claims=jWTUtil.validateToken(refreshToken);
        String newAccessToken=jWTUtil.generateToken(claims, 1); //테스트 하려고 1분 설정
        String newRefreshToken=refreshToken;
        if (checkTime((Long)claims.get("exp"))){
            newRefreshToken= jWTUtil.generateToken(claims, 60*2);
        }

        return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken);
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
            jWTUtil.validateToken(token);
        }catch (CustomJWTException ex){
            System.out.println("checkExpiredToken ==> " + ex.getMessage());
            if (ex.getMessage().equals("Expired")){
                return true;
            }
        }
        return false;
    }
}
