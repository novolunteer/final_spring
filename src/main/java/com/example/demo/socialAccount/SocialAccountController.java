package com.example.demo.socialAccount;

import com.example.demo.security.jwtutil.JWTUtil;
import com.example.demo.socialAccount.dto.NaverJoinResponse;
import com.example.demo.socialAccount.dto.NaverTokenResponse;
import com.example.demo.socialAccount.dto.NaverUserInfoResponse;
import com.example.demo.user.UserDto;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @Value("${react.uri}")
    private String reactUri;

    private final SocialAccountService socialAccountService;
    private final JWTUtil jwtUtil;

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
            response.sendRedirect(reactUri + "/login?error=invalid_state");
            return;
        }

        NaverTokenResponse tokenResponse=socialAccountService.getAccessToken(code, state);
        NaverUserInfoResponse userInfoResponse=socialAccountService.getUserInfo(tokenResponse.getAccessToken());

        String providerId=userInfoResponse.getResponse().getId();

        UserDto user=socialAccountService.verifyUser(SocialAccountProvider.NAVER, providerId);
        if (user == null){
            session.setAttribute("socialProvider", "NAVER");
            session.setAttribute("socialProviderId", providerId);
            session.setAttribute("socialName", userInfoResponse.getResponse().getName());
            session.setAttribute("socialGender", userInfoResponse.getResponse().getGender() == null ?
                    null : userInfoResponse.getResponse().getGender());
            session.setAttribute("socialMobile", userInfoResponse.getResponse().getMobile() != null ?
                    userInfoResponse.getResponse().getMobile() : (
                            userInfoResponse.getResponse().getMobile_e164() != null ?
                                    userInfoResponse.getResponse().getMobile_e164() : null
                    ));

            response.sendRedirect(naverLoginUri);
            return;
        }

        Map<String, Object> claims=new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("userId", user.getUserId());
        claims.put("roles", user.getRoles());
        claims.put("status", user.getStatus());

        String accessToken= jwtUtil.generateToken(claims, 5);
        String refreshToken=jwtUtil.generateToken(claims, 60*2);

        String redirectUrl=reactUri + "/login?accessToken="
                + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                + "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/social/login/naver/info")
    public ResponseEntity<NaverJoinResponse> getNaverUserInfo(HttpSession session){
        try{
            String phone=null;
            if (session.getAttribute("mobile") != null){
                List<String> mobile=List.of(session.getAttribute("mobile").toString().split("-"));
                for (String s:mobile){
                    phone += s;
                }
            }

            return ResponseEntity.ok(NaverJoinResponse.builder()
                    .provider(session.getAttribute("socialProvider").toString())
                    .providerId(session.getAttribute("socialProviderId").toString())
                    .name(session.getAttribute("socialName").toString())
                    .gender(session.getAttribute("socialGender").toString())
                    .mobile(phone).build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
