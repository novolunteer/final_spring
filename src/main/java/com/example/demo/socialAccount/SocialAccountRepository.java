package com.example.demo.socialAccount;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Integer> {
    SocialAccount findByProviderAndProviderId(SocialAccountProvider provider, String providerId);
}
