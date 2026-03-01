package com.example.demo.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {

        String username = CurrentUser.get();

        if (username == null || username.isBlank()) {
            return Optional.of("SYSTEM");
        }

        return Optional.of(username);
    }
}