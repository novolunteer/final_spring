package com.example.demo.socialAccount;

import com.example.demo.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Integer> {
    SocialAccount findByProviderAndProviderId(SocialAccountProvider provider, String providerId);
    List<SocialAccount> findByUser(User user);
}
