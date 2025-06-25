package com.tiny.oauthserver.sys.service;

import com.tiny.oauthserver.sys.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface RoleService {
    Page<RoleResponseDto> roles(RoleRequestDto query, Pageable pageable);
    Optional<Role> findById(Long id);
    RoleResponseDto create(RoleCreateUpdateDto dto);
    RoleResponseDto update(Long id, RoleCreateUpdateDto dto);
    void delete(Long id);
} 