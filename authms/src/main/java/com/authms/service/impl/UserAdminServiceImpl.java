package com.authms.service.impl;

import com.authms.dto.response.AdminUserResponse;
import com.authms.entity.AppUser;
import com.authms.entity.Role;
import com.authms.repository.AppUserRepository;
import com.authms.service.UserAdminService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final AppUserRepository appUserRepository;

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminUserResponse> findAllUsers() {
        return appUserRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AdminUserResponse toResponse(AppUser user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles().stream().map(Role::name).collect(Collectors.toSet()),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}
