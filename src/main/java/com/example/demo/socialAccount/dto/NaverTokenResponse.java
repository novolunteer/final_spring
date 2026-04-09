package com.example.demo.socialAccount.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NaverTokenResponse {
    private String accessToken;
    private String refreshToken;
    private String expiresIn;
    private String error;
    private String errorDescription;
}
