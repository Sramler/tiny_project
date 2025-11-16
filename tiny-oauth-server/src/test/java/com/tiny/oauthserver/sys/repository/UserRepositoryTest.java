package com.tiny.oauthserver.sys.repository;

import com.tiny.oauthserver.sys.enums.ResourceType;
import com.tiny.oauthserver.sys.model.Resource;
import com.tiny.oauthserver.sys.model.Role;
import com.tiny.oauthserver.sys.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserRepositoryTest {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);

        // 创建 mock 实体
        Resource resource = new Resource();
        resource.setName("用户新增权限");
        resource.setUrl("/api/user/add");
        resource.setMethod("POST");
        resource.setType(ResourceType.fromCode(0));
        resource.setParentId(0L);

        Role role = new Role();
        role.setName("ROLE_TEST");
        role.setDescription("测试角色");
        role.setResources(Set.of(resource));

        User user = new User();
        user.setUsername("test");
        user.setPassword("password");
        user.setRoles(Set.of(role));

        when(userRepository.findUserByUsername("test"))
                .thenReturn(Optional.of(user));
    }

    @Test
    void testFindUserByUsername() {
        Optional<User> optionalUser = userRepository.findUserByUsername("test");

        assertThat(optionalUser).isPresent();
        User user = optionalUser.get();

        assertThat(user.getUsername()).isEqualTo("test");
        assertThat(user.getRoles()).hasSize(1);

        Role role = user.getRoles().iterator().next();
        assertThat(role.getName()).isEqualTo("ROLE_TEST");

        Resource resource = role.getResources().iterator().next();
        assertThat(resource.getUrl()).isEqualTo("/api/user/add");
    }

}