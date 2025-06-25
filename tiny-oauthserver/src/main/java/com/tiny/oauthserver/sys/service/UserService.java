package com.tiny.oauthserver.sys.service;

import com.tiny.oauthserver.sys.model.User;
import com.tiny.oauthserver.sys.model.UserRequestDto;
import com.tiny.oauthserver.sys.model.UserResponseDto;
import com.tiny.oauthserver.sys.model.UserCreateUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Page<UserResponseDto> users(UserRequestDto query, Pageable pageable);
    Optional<User> findById(Long id);
    User createFromDto(UserCreateUpdateDto userDto);
    User updateFromDto(UserCreateUpdateDto userDto);
    void delete(Long id);
    // 批量操作方法
    void batchEnable(List<Long> ids);
    void batchDisable(List<Long> ids);
    void batchDelete(List<Long> ids);
} 