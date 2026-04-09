package com.example.demo.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SseService {
    private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();

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
}
