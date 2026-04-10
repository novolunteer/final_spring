package com.example.demo.socialAccount;

import com.example.demo.security.jwtutil.JWTUtil;
import com.example.demo.socialAccount.dto.*;
import com.example.demo.user.User;
import com.example.demo.user.UserDto;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

        NaverTokenResponse tokenResponse=socialAccountService.getNaverAccessToken(code, state);
        NaverUserInfoResponse userInfoResponse=socialAccountService.getNaverUserInfo(tokenResponse.getAccess_token());

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

        String accessToken=jwtUtil.generateToken(claims, 5);
        String refreshToken=jwtUtil.generateToken(claims, 60*2);

        session.setAttribute("email", user.getEmail());
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("roles", user.getRoles());
        session.setAttribute("status", user.getStatus());
        session.setAttribute("accessToken", accessToken);
        session.setAttribute("refreshToken", refreshToken);

        //여기서 로그인 어떻게 처리할지 고민해야 함
        response.sendRedirect(reactUri + "/login?mode=naverLogin");
    }

    @GetMapping("/social/login/naver/info")
    public ResponseEntity<NaverLoginResponse> getNaverUserInfo(HttpSession session){
        try{
            String phone=null;
            if (session.getAttribute("mobile") != null){
                String mobile=(String) session.getAttribute("mobile");
                phone=mobile.replace("-","");
            }

            return ResponseEntity.ok(NaverLoginResponse.builder()
                    .provider((String) session.getAttribute("socialProvider"))
                    .providerId((String) session.getAttribute("socialProviderId"))
                    .name((String) session.getAttribute("socialName"))
                    .gender(session.getAttribute("socialGender") == null ? null : (String) session.getAttribute("socialGender"))
                    .mobile(phone).build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/social/login/naver/complete")
    public ResponseEntity<SocialLoginResponse> naverLogin(@RequestBody NaverLoginRequest request,
                                                          HttpSession session){
        try{
            User user=socialAccountService.registerNaverAccount(request);

            Map<String, Object> claims=new HashMap<>();
            claims.put("email", user.getEmail());
            claims.put("userId", user.getUserId());
            claims.put("roles", user.getUserRoles());
            claims.put("status", user.getStatus());

            String accessToken=jwtUtil.generateToken(claims, 5);
            String refreshToken=jwtUtil.generateToken(claims, 60*2);

            List<String> roles=user.getUserRoles().stream().map(r -> r.getRole().getRoleName()).toList();

            SocialLoginResponse response=SocialLoginResponse.builder()
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .roles(roles)
                    .status(user.getStatus())
                    .departmentId(null).build();

            session.invalidate();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/social/login/naver/complete")
    public ResponseEntity<SocialLoginResponse> naverUserLogin(HttpSession session){
        try{
            SocialLoginResponse response=SocialLoginResponse.builder()
                    .userId((Integer) session.getAttribute("userId"))
                    .email((String) session.getAttribute("email"))
                    .status((String) session.getAttribute("status"))
                    .roles((List<String>) session.getAttribute("roles"))
                    .accessToken((String) session.getAttribute("accessToken"))
                    .refreshToken((String) session.getAttribute("refreshToken")).build();

            session.invalidate();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
