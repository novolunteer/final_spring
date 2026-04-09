package com.example.demo.sse;

import com.example.demo.security.jwtutil.CustomJWTException;
import com.example.demo.security.jwtutil.JWTUtil;
import com.example.demo.staff.Staff;
import com.example.demo.staff.StaffRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
public class SseController {
    private final SseService sseService;
    private final JWTUtil jWTUtil;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;

    @GetMapping(value = "/api/sse/subscribe/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Integer userId,
                                @RequestParam(value = "token", required = false) String token) {

        SseEmitter emitter = new SseEmitter(60 * 1000L);

        try {
            if (token == null || token.isBlank()) {
                emitter.send(SseEmitter.event().name("error").data("NO_TOKEN"));
                emitter.complete();
                return emitter;
            }

            Claims claims=null;
            try {
                claims = jWTUtil.validateToken(token);
            } catch (CustomJWTException e) {
                if ("Expired".equals(e.getMessage())) {
                    System.out.println("토큰 만료지만 진행");
                    claims = jWTUtil.getClaimsIgnoreExpiration(token);
                } else {
                    throw e;
                }
            }

            Integer claimUserId = Integer.valueOf(claims.get("userId").toString());
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("user Not exist"));
            Staff staff = staffRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("staff Not exist"));

            System.out.println("로그인한 의사===========>"+userId);


            if (!staff.getUser().getUserId().equals(claimUserId)) {
                emitter.send(SseEmitter.event().name("error").data("FORBIDDEN"));
                emitter.complete();
                return emitter;
            }
            System.out.println("SSE 구독 요청 userId = " + userId);

            return sseService.subscribe(userId);

        } catch (Exception e) {
            try {
                e.printStackTrace();
                emitter.send(SseEmitter.event().name("error").data("AUTH_ERROR"));
            } catch (Exception ignored) {
                System.out.println(ignored.getMessage());
            }
            emitter.complete();
            return emitter;
        }

    }
}
