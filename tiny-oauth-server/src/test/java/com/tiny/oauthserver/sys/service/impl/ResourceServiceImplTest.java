package com.tiny.oauthserver.sys.service.impl;

import com.tiny.oauthserver.sys.model.Resource;
import com.tiny.oauthserver.sys.model.ResourceCreateUpdateDto;
import com.tiny.oauthserver.sys.repository.ResourceRepository;
import com.tiny.oauthserver.sys.repository.UserRepository;
import com.tiny.oauthserver.sys.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ResourceServiceImplTest {

    private final ResourceRepository resourceRepository = Mockito.mock(ResourceRepository.class);
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
    private final ResourceServiceImpl service = new ResourceServiceImpl(resourceRepository, userRepository, roleRepository);

    @Test
    @DisplayName("createFromDto: 名称重复抛出异常")
    void createFromDto_nameExists() {
        ResourceCreateUpdateDto dto = new ResourceCreateUpdateDto();
        dto.setName("menu_home");
        when(resourceRepository.findByName("menu_home")).thenReturn(Optional.of(new Resource()));

        assertThrows(RuntimeException.class, () -> service.createFromDto(dto));
    }

    @Test
    @DisplayName("update: 找不到资源抛出异常")
    void update_notFound() {
        when(resourceRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.update(999L, new Resource()));
    }

    @Test
    @DisplayName("updateSort: 正常更新排序")
    void updateSort_ok() {
        Resource r = new Resource();
        r.setId(1L);
        r.setSort(1);
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(r));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(inv -> inv.getArgument(0));

        Resource saved = service.updateSort(1L, 5);
        assertThat(saved.getSort()).isEqualTo(5);
        verify(resourceRepository).save(any(Resource.class));
    }
}


