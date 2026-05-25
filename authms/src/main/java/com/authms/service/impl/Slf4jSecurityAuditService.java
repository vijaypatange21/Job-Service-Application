package com.authms.service.impl;

import com.authms.entity.AppUser;
import com.authms.service.SecurityAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Slf4jSecurityAuditService implements SecurityAuditService {

    @Override
    public void loginSucceeded(AppUser user) {
        log.info("security_event=login_succeeded user_id={} email={}", user.getId(), user.getEmail());
    }

    @Override
    public void loginFailed(String email) {
        log.warn("security_event=login_failed email={}", email);
    }

    @Override
    public void tokenRefreshed(AppUser user) {
        log.info("security_event=token_refreshed user_id={} email={}", user.getId(), user.getEmail());
    }

    @Override
    public void logoutSucceeded(String email) {
        log.info("security_event=logout_succeeded email={}", email);
    }
}
