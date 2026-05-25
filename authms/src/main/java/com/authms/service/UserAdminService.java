package com.authms.service;

import com.authms.dto.response.AdminUserResponse;
import java.util.List;

public interface UserAdminService {

    List<AdminUserResponse> findAllUsers();
}
