package com.example.demo.socialAccount;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SocialAccountController {
    @Value("${naver.client-id}")
    private String naverClientId;
    @Value("${naver.redirect-uri}")
    private String naverRedirectUri;
    @Value("${naver.login.uri}")
    private String naverLoginUri;

    @GetMapping("/social/login/naver")
    public void redirectToNaver(HttpServletResponse response, HttpSession session) throws IOException {
        String state= UUID.randomUUID().toString();
        session.setAttribute("naver_oauth_state", state);

        String naverAuthUrl=
                "https://nid.naver.com/oauth2.0/authorize" +
                        "?response_type=code" +
                        "&client_id=" + URLEncoder.encode(naverClientId, StandardCharsets.UTF_8) +
                        "&redirect_uri=" + URLEncoder.encode(naverRedirectUri, StandardCharsets.UTF_8) +
                        "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);

        response.sendRedirect(naverAuthUrl);
    }

    @GetMapping("/social/login/naver/callback")
    public void naverCallback(@RequestParam("code") String code,
                              @RequestParam("state") String state,
                              HttpSession session, HttpServletResponse response) throws IOException {
        String savedState=(String) session.getAttribute("naver_oauth_state");
        if (savedState == null || !savedState.equals(state)){
            throw new RemoteException("잘못된 접근입니다. state 값이 일치하지 않습니다.");
        }
    }
}
