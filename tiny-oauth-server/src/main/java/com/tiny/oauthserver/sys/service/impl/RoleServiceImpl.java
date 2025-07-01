package com.tiny.oauthserver.sys.service.impl;

import com.tiny.oauthserver.sys.model.*;
import com.tiny.oauthserver.sys.repository.RoleRepository;
import com.tiny.oauthserver.sys.repository.UserRepository;
import com.tiny.oauthserver.sys.service.RoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    public RoleServiceImpl(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Page<RoleResponseDto> roles(RoleRequestDto query, Pageable pageable) {
        Specification<Role> spec = (root, cq, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            
            if (query.getName() != null && !query.getName().trim().isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + query.getName().trim() + "%"));
            }
            
            if (query.getCode() != null && !query.getCode().trim().isEmpty()) {
                predicates.add(cb.like(root.get("code"), "%" + query.getCode().trim() + "%"));
            }
            
            if (predicates.isEmpty()) {
                return cb.conjunction();
            } else {
                return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
            }
        };
        Page<Role> page = roleRepository.findAll(spec, pageable);
        return page.map(this::toDto);
    }

    @Override
    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    @Override
    public RoleResponseDto create(RoleCreateUpdateDto dto) {
        Role role = new Role();
        BeanUtils.copyProperties(dto, role);
        if (dto.getUserIds() != null) {
            List<User> users = dto.getUserIds().stream()
                .map(uid -> userRepository.findById(uid).orElse(null))
                .filter(Objects::nonNull)
                .toList();
            role.setUsers(new HashSet<>(users));
        }
        return toDto(roleRepository.save(role));
    }

    @Override
    public RoleResponseDto update(Long id, RoleCreateUpdateDto dto) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new RuntimeException("角色不存在"));
        BeanUtils.copyProperties(dto, role, "id");

        // 只查当前拥有该角色的用户
        List<User> oldUsers = userRepository.findByRoleId(id);
        for (User user : oldUsers) {
            user.getRoles().removeIf(r -> r.getId().equals(id));
            userRepository.save(user);
        }

        // 再为新分配的用户添加角色
        if (dto.getUserIds() != null) {
            for (Long userId : dto.getUserIds()) {
                userRepository.findById(userId).ifPresent(user -> {
                    user.getRoles().add(role);
                    userRepository.save(user);
                });
            }
        }

        return toDto(roleRepository.save(role));
    }

    @Override
    public void delete(Long id) {
        roleRepository.deleteById(id);
    }

    @Override
    public List<Long> getUserIdsByRoleId(Long roleId) {
        Optional<Role> roleOpt = roleRepository.findByIdFetchUsers(roleId);
        if (roleOpt.isPresent()) {
            Role role = roleOpt.get();
            if (role.getUsers() != null) {
                return role.getUsers().stream().map(User::getId).toList();
            }
        }
        return List.of();
    }

    @Override
    public void updateRoleUsers(Long roleId, List<Long> userIds) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty()) return;
        Role role = roleOpt.get();
        List<User> users = new ArrayList<>();
        if (userIds != null && !userIds.isEmpty()) {
            users = userIds.stream()
                    .map(uid -> userRepository.findById(uid).orElse(null))
                    .filter(u -> u != null)
                    .toList();
        }
        role.setUsers(new HashSet<>(users));
        roleRepository.save(role);
    }

    private RoleResponseDto toDto(Role role) {
        RoleResponseDto dto = new RoleResponseDto();
        BeanUtils.copyProperties(role, dto);
        return dto;
    }
} 