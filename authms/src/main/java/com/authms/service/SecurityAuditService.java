package com.authms.service;

import com.authms.entity.AppUser;

public interface SecurityAuditService {

    void loginSucceeded(AppUser user);

    void loginFailed(String email);

    void tokenRefreshed(AppUser user);

    void logoutSucceeded(String email);
}
