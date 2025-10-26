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
    Optional<User> findByUsername(String username);
    User create(User user);
    User update(Long id, User user);
    User createFromDto(UserCreateUpdateDto userDto);
    User updateFromDto(UserCreateUpdateDto userDto);
    void delete(Long id);
    
    // 批量操作方法
    void batchEnable(List<Long> ids);
    void batchDisable(List<Long> ids);
    void batchDelete(List<Long> ids);

    /**
     * 更新用户角色绑定
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void updateUserRoles(Long userId, List<Long> roleIds);
}