package com.tiny.oauthserver.sys.service.impl;

import com.tiny.oauthserver.sys.model.Resource;
import com.tiny.oauthserver.sys.model.Role;
import com.tiny.oauthserver.sys.model.RoleCreateUpdateDto;
import com.tiny.oauthserver.sys.model.User;
import com.tiny.oauthserver.sys.repository.ResourceRepository;
import com.tiny.oauthserver.sys.repository.RoleRepository;
import com.tiny.oauthserver.sys.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RoleServiceImplUpdateTest {

    private final RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final ResourceRepository resourceRepository = Mockito.mock(ResourceRepository.class);
    private final RoleServiceImpl service = new RoleServiceImpl(roleRepository, userRepository, resourceRepository);

    @Test
    @DisplayName("update: 角色不存在抛出异常")
    void update_notFound() {
        when(roleRepository.findById(100L)).thenReturn(Optional.empty());
        RoleCreateUpdateDto dto = new RoleCreateUpdateDto();
        assertThrows(RuntimeException.class, () -> service.update(100L, dto));
    }

    @Test
    @DisplayName("update: 清空旧用户并添加新用户，最终保存")
    void update_shouldReplaceUsers() {
        Role role = new Role();
        role.setId(1L);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userRepository.findUserIdsByRoleId(1L)).thenReturn(List.of(10L, 11L));
        when(userRepository.findById(10L)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(11L)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(20L)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(21L)).thenReturn(Optional.of(new User()));
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        RoleCreateUpdateDto dto = new RoleCreateUpdateDto();
        dto.setUserIds(List.of(20L, 21L));

        service.update(1L, dto);

        verify(userRepository, atLeastOnce()).save(any(User.class));
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("updateRoleUsers: 重建用户-角色关联，仅保留传入用户")
    void updateRoleUsers_rebuildRelations() {
        Role role = new Role();
        role.setId(2L);
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role));
        when(userRepository.findUserIdsByRoleId(2L)).thenReturn(List.of(30L, 31L));
        when(userRepository.findById(40L)).thenReturn(Optional.of(new User()));

        service.updateRoleUsers(2L, List.of(40L));

        verify(userRepository).deleteUserRoleRelation(30L, 2L);
        verify(userRepository).deleteUserRoleRelation(31L, 2L);
        verify(userRepository).addUserRoleRelation(40L, 2L);
    }

    @Test
    @DisplayName("updateRoleResources: 更新角色资源为给定集合")
    void updateRoleResources_setResources() {
        Role role = new Role();
        role.setId(3L);
        when(roleRepository.findById(3L)).thenReturn(Optional.of(role));
        when(resourceRepository.findAllById(List.of(5L, 6L))).thenReturn(List.of(new Resource(), new Resource()));

        service.updateRoleResources(3L, List.of(5L, 6L));

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());
        Role saved = captor.getValue();
        assertThat(saved.getResources()).hasSize(2);
    }
}
