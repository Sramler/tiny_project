package com.tiny.oauthserver.sys.service.impl;

import com.tiny.oauthserver.sys.model.*;
import com.tiny.oauthserver.sys.repository.RoleRepository;
import com.tiny.oauthserver.sys.service.RoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    public RoleServiceImpl(RoleRepository roleRepository) { this.roleRepository = roleRepository; }

    @Override
    public Page<RoleResponseDto> roles(RoleRequestDto query, Pageable pageable) {
        Specification<Role> spec = (root, cq, cb) -> {
            if (query.getName() != null && !query.getName().trim().isEmpty()) {
                return cb.like(root.get("name"), "%" + query.getName().trim() + "%");
            }
            return cb.conjunction();
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
        return toDto(roleRepository.save(role));
    }

    @Override
    public RoleResponseDto update(Long id, RoleCreateUpdateDto dto) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new RuntimeException("角色不存在"));
        BeanUtils.copyProperties(dto, role, "id");
        return toDto(roleRepository.save(role));
    }

    @Override
    public void delete(Long id) {
        roleRepository.deleteById(id);
    }

    private RoleResponseDto toDto(Role role) {
        RoleResponseDto dto = new RoleResponseDto();
        BeanUtils.copyProperties(role, dto);
        return dto;
    }
} 