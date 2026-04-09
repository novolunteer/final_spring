package com.example.demo.socialAccount;

import com.example.demo.patient.Patient;
import com.example.demo.patient.PatientRepository;
import com.example.demo.socialAccount.dto.NaverTokenResponse;
import com.example.demo.socialAccount.dto.NaverUserInfoResponse;
import com.example.demo.user.User;
import com.example.demo.user.UserDto;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class SocialAccountService {
    @Value("${naver.client-id}")
    private String naverClientId;
    @Value("${naver.client-secret}")
    private String naverClientSecret;
    @Value("${naver.redirect-uri}")
    private String naverRedirectUri;

    private final RestTemplate restTemplate=new RestTemplate();
    private final SocialAccountRepository socialAccountRepository;

    public UserDto verifyUser(SocialAccountProvider provider, String providerId){
        SocialAccount account=socialAccountRepository.findByProviderAndProviderId(provider, providerId);
        if (account != null){
            return new UserDto(account.getUser());
        } else {
            return null;
        }
    }

    public NaverUserInfoResponse getUserInfo(String accessToken){
        String url = "https://openapi.naver.com/v1/nid/me";

        HttpHeaders headers=new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity=new HttpEntity<>(headers);

        ResponseEntity<NaverUserInfoResponse> response=restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                NaverUserInfoResponse.class
        );
        return response.getBody();
    }

    public NaverTokenResponse getAccessToken(String code, String state){
        String url = "https://nid.naver.com/oauth2.0/token" +
                "?grant_type=authorization_code" +
                "&client_id=" + naverClientId +
                "&client_secret=" + naverClientSecret +
                "&code=" + code +
                "&state=" + state;

        ResponseEntity<NaverTokenResponse> response=
                restTemplate.getForEntity(url, NaverTokenResponse.class);

        return response.getBody();
    }
}
