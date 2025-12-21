package com.tiny.web.sys.service.impl;

import com.tiny.web.sys.ResourceService;
import com.tiny.web.sys.repository.ResourceRepository;
import com.tiny.web.sys.model.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;

    @Override
    public boolean hasAccess(String role, String path, String method) {
        // 你可以根据角色去查拥有的资源权限，这里是简单模拟
        List<Resource> resources = resourceRepository.findByRoleName(role);
        return resources.stream().anyMatch(res ->
            path.equalsIgnoreCase(res.getPath()) &&
            method.equalsIgnoreCase(res.getMethod())
        );
    }
}