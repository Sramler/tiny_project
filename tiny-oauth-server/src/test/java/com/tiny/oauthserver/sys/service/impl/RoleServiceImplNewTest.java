package com.tiny.oauthserver.sys.service.impl;

import com.tiny.oauthserver.sys.model.Role;
import com.tiny.oauthserver.sys.model.RoleCreateUpdateDto;
import com.tiny.oauthserver.sys.model.RoleResponseDto;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RoleServiceImplNewTest {

    private final RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final ResourceRepository resourceRepository = Mockito.mock(ResourceRepository.class);
    private final RoleServiceImpl service = new RoleServiceImpl(roleRepository, userRepository, resourceRepository);

    @Test
    @DisplayName("create: 复制属性并保存，返回DTO")
    void create_shouldCopyAndSave() {
        RoleCreateUpdateDto dto = new RoleCreateUpdateDto();
        dto.setName("Editor");
        dto.setCode("editor");
        dto.setUserIds(List.of(1L, 2L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> {
            Role r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });

        RoleResponseDto resp = service.create(dto);

        assertThat(resp.getId()).isEqualTo(10L);
        assertThat(resp.getName()).isEqualTo("Editor");
        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());
        Role saved = captor.getValue();
        assertThat(saved.getUsers()).hasSize(2);
    }

    @Test
    @DisplayName("delete: 调用仓库删除")
    void delete_shouldDelegate() {
        service.delete(7L);
        verify(roleRepository).deleteById(7L);
    }
}


