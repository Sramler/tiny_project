package com.tiny.oauthserver.sys.service.impl;

import com.tiny.oauthserver.sys.enums.ResourceType;
import com.tiny.oauthserver.sys.model.*;
import com.tiny.oauthserver.sys.repository.ResourceRepository;
import com.tiny.oauthserver.sys.service.ResourceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 资源服务实现类
 */
@Service
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceServiceImpl(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Override
    public Page<ResourceResponseDto> resources(ResourceRequestDto query, Pageable pageable) {
        // 构建查询条件
        ResourceType type = query.getType() != null ? ResourceType.fromCode(query.getType()) : null;
        
        // 使用复合查询方法
        Page<Resource> resourcePage = resourceRepository.findByConditions(
                query.getName(),
                query.getUrl(),
                query.getUri(),
                query.getPermission(),
                query.getTitle(),
                type,
                query.getParentId(),
                query.getHidden(),
                pageable
        );
        
        // 转换为DTO
        Page<ResourceResponseDto> dtoPage = resourcePage.map(this::toDto);
        
        return dtoPage;
    }

    @Override
    public Optional<Resource> findById(Long id) {
        return resourceRepository.findById(id);
    }

    @Override
    public Resource create(Resource resource) {
        return resourceRepository.save(resource);
    }

    @Override
    public Resource update(Long id, Resource resource) {
        Resource existingResource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("资源不存在"));
        
        // 更新字段
        existingResource.setName(resource.getName());
        existingResource.setTitle(resource.getTitle());
        existingResource.setUrl(resource.getUrl());
        existingResource.setUri(resource.getUri());
        existingResource.setMethod(resource.getMethod());
        existingResource.setIcon(resource.getIcon());
        existingResource.setShowIcon(resource.getShowIcon());
        existingResource.setSort(resource.getSort());
        existingResource.setComponent(resource.getComponent());
        existingResource.setRedirect(resource.getRedirect());
        existingResource.setHidden(resource.getHidden());
        existingResource.setKeepAlive(resource.getKeepAlive());
        existingResource.setPermission(resource.getPermission());
        existingResource.setType(resource.getType());
        existingResource.setParentId(resource.getParentId());
        
        return resourceRepository.save(existingResource);
    }

    @Override
    public Resource createFromDto(ResourceCreateUpdateDto resourceDto) {
        // 检查名称是否已存在
        if (resourceRepository.findByName(resourceDto.getName()).isPresent()) {
            throw new RuntimeException("资源名称已存在");
        }
        
        // 检查URL是否已存在（如果提供了URL）
        if (StringUtils.hasText(resourceDto.getUrl()) && 
            resourceRepository.findByUrl(resourceDto.getUrl()).isPresent()) {
            throw new RuntimeException("资源URL已存在");
        }
        
        // 检查URI是否已存在（如果提供了URI）
        if (StringUtils.hasText(resourceDto.getUri()) && 
            resourceRepository.findByUri(resourceDto.getUri()).isPresent()) {
            throw new RuntimeException("资源URI已存在");
        }
        
        // 创建资源对象
        Resource resource = new Resource();
        resource.setName(resourceDto.getName());
        resource.setTitle(resourceDto.getTitle());
        resource.setUrl(resourceDto.getUrl());
        
        // 设置 uri 和 method 的默认值
        String uri = resourceDto.getUri();
        if (uri == null || uri.trim().isEmpty()) {
            uri = ""; // 设置默认值为空字符串
        }
        resource.setUri(uri);
        
        String method = resourceDto.getMethod();
        if (method == null || method.trim().isEmpty()) {
            method = ""; // 设置默认值为空字符串
        }
        resource.setMethod(method);
        
        resource.setIcon(resourceDto.getIcon());
        resource.setShowIcon(resourceDto.isShowIcon());
        resource.setSort(resourceDto.getSort());
        resource.setComponent(resourceDto.getComponent());
        resource.setRedirect(resourceDto.getRedirect());
        resource.setHidden(resourceDto.isHidden());
        resource.setKeepAlive(resourceDto.isKeepAlive());
        resource.setPermission(resourceDto.getPermission());
        resource.setType(ResourceType.fromCode(resourceDto.getType()));
        resource.setParentId(resourceDto.getParentId());
        
        return resourceRepository.save(resource);
    }

    @Override
    public Resource updateFromDto(ResourceCreateUpdateDto resourceDto) {
        Resource existingResource = resourceRepository.findById(resourceDto.getId())
                .orElseThrow(() -> new RuntimeException("资源不存在"));
        
        // 检查名称是否已被其他资源使用
        Optional<Resource> resourceWithSameName = resourceRepository.findByName(resourceDto.getName());
        if (resourceWithSameName.isPresent() && !resourceWithSameName.get().getId().equals(resourceDto.getId())) {
            throw new RuntimeException("资源名称已被其他资源使用");
        }
        
        // 检查URL是否已被其他资源使用（如果提供了URL）
        if (StringUtils.hasText(resourceDto.getUrl())) {
            Optional<Resource> resourceWithSameUrl = resourceRepository.findByUrl(resourceDto.getUrl());
            if (resourceWithSameUrl.isPresent() && !resourceWithSameUrl.get().getId().equals(resourceDto.getId())) {
                throw new RuntimeException("资源URL已被其他资源使用");
            }
        }
        
        // 检查URI是否已被其他资源使用（如果提供了URI）
        if (StringUtils.hasText(resourceDto.getUri())) {
            Optional<Resource> resourceWithSameUri = resourceRepository.findByUri(resourceDto.getUri());
            if (resourceWithSameUri.isPresent() && !resourceWithSameUri.get().getId().equals(resourceDto.getId())) {
                throw new RuntimeException("资源URI已被其他资源使用");
            }
        }
        
        // 更新字段
        existingResource.setName(resourceDto.getName());
        existingResource.setTitle(resourceDto.getTitle());
        existingResource.setUrl(resourceDto.getUrl());
        
        // 设置 uri 和 method 的默认值
        String uri = resourceDto.getUri();
        if (uri == null || uri.trim().isEmpty()) {
            uri = ""; // 设置默认值为空字符串
        }
        existingResource.setUri(uri);
        
        String method = resourceDto.getMethod();
        if (method == null || method.trim().isEmpty()) {
            method = ""; // 设置默认值为空字符串
        }
        existingResource.setMethod(method);
        
        existingResource.setIcon(resourceDto.getIcon());
        existingResource.setShowIcon(resourceDto.isShowIcon());
        existingResource.setSort(resourceDto.getSort());
        existingResource.setComponent(resourceDto.getComponent());
        existingResource.setRedirect(resourceDto.getRedirect());
        existingResource.setHidden(resourceDto.isHidden());
        existingResource.setKeepAlive(resourceDto.isKeepAlive());
        existingResource.setPermission(resourceDto.getPermission());
        existingResource.setType(ResourceType.fromCode(resourceDto.getType()));
        existingResource.setParentId(resourceDto.getParentId());
        
        return resourceRepository.save(existingResource);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 检查是否有子资源
        List<Resource> children = resourceRepository.findByParentIdOrderBySortAsc(id);
        if (!children.isEmpty()) {
            throw new RuntimeException("无法删除有子资源的资源，请先删除子资源");
        }
        
        resourceRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        // 检查是否有资源包含子资源
        for (Long id : ids) {
            List<Resource> children = resourceRepository.findByParentIdOrderBySortAsc(id);
            if (!children.isEmpty()) {
                throw new RuntimeException("无法删除有子资源的资源，请先删除子资源");
            }
        }
        
        resourceRepository.deleteAllById(ids);
    }

    @Override
    public List<Resource> findByType(ResourceType type) {
        return resourceRepository.findByTypeOrderBySortAsc(type);
    }

    @Override
    public List<Resource> findByTypeIn(List<ResourceType> types) {
        return resourceRepository.findByTypeInOrderBySortAsc(types);
    }

    @Override
    public List<Resource> findByParentId(Long parentId) {
        return resourceRepository.findByParentIdOrderBySortAsc(parentId);
    }

    @Override
    public List<Resource> findTopLevel() {
        return resourceRepository.findByParentIdIsNullOrderBySortAsc();
    }

    @Override
    public List<ResourceResponseDto> buildResourceTree(List<Resource> resources) {
        // 构建ID到资源的映射
        Map<Long, ResourceResponseDto> resourceMap = new HashMap<>();
        List<ResourceResponseDto> rootResources = new ArrayList<>();
        
        // 转换为DTO并建立映射
        for (Resource resource : resources) {
            ResourceResponseDto dto = toDto(resource);
            resourceMap.put(resource.getId(), dto);
        }
        
        // 构建树形结构
        for (Resource resource : resources) {
            ResourceResponseDto dto = resourceMap.get(resource.getId());
            if (resource.getParentId() == null) {
                // 顶级资源
                rootResources.add(dto);
            } else {
                // 子资源
                ResourceResponseDto parent = resourceMap.get(resource.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new HashSet<>());
                    }
                    parent.getChildren().add(dto);
                }
            }
        }
        
        return rootResources;
    }

    @Override
    public Optional<Resource> findByName(String name) {
        return resourceRepository.findByName(name);
    }

    @Override
    public Optional<Resource> findByUrl(String url) {
        return resourceRepository.findByUrl(url);
    }

    @Override
    public Optional<Resource> findByUri(String uri) {
        return resourceRepository.findByUri(uri);
    }

    @Override
    public List<Resource> findByPermission(String permission) {
        return resourceRepository.findByPermission(permission);
    }

    @Override
    public boolean existsByName(String name, Long excludeId) {
        if (excludeId == null) {
            return resourceRepository.findByName(name).isPresent();
        }
        return resourceRepository.existsByNameAndIdNot(name, excludeId);
    }

    @Override
    public boolean existsByUrl(String url, Long excludeId) {
        if (excludeId == null) {
            return resourceRepository.findByUrl(url).isPresent();
        }
        return resourceRepository.existsByUrlAndIdNot(url, excludeId);
    }

    @Override
    public boolean existsByUri(String uri, Long excludeId) {
        if (excludeId == null) {
            return resourceRepository.findByUri(uri).isPresent();
        }
        return resourceRepository.existsByUriAndIdNot(uri, excludeId);
    }

    @Override
    public Resource updateSort(Long id, Integer sort) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("资源不存在"));
        resource.setSort(sort);
        return resourceRepository.save(resource);
    }

    @Override
    public List<ResourceType> getResourceTypes() {
        return Arrays.asList(ResourceType.values());
    }

    @Override
    public List<Resource> findByRoleId(Long roleId) {
        // TODO: 实现根据角色ID查找资源的逻辑
        // 这需要关联查询用户-角色-资源表
        return new ArrayList<>();
    }

    @Override
    public List<Resource> findByUserId(Long userId) {
        // TODO: 实现根据用户ID查找资源的逻辑
        // 这需要关联查询用户-角色-资源表
        return new ArrayList<>();
    }

    /**
     * 将Resource实体转换为ResourceResponseDto
     */
    private ResourceResponseDto toDto(Resource resource) {
        ResourceResponseDto dto = new ResourceResponseDto();
        dto.setId(resource.getId());
        dto.setName(resource.getName());
        dto.setTitle(resource.getTitle());
        dto.setUrl(resource.getUrl());
        dto.setUri(resource.getUri());
        dto.setMethod(resource.getMethod());
        dto.setIcon(resource.getIcon());
        dto.setShowIcon(resource.getShowIcon());
        dto.setSort(resource.getSort());
        dto.setComponent(resource.getComponent());
        dto.setRedirect(resource.getRedirect());
        dto.setHidden(resource.getHidden());
        dto.setKeepAlive(resource.getKeepAlive());
        dto.setPermission(resource.getPermission());
        dto.setType(resource.getType().getCode());
        dto.setTypeName(getTypeName(resource.getType()));
        dto.setParentId(resource.getParentId());
        dto.setChildren(new HashSet<>());
        return dto;
    }

    /**
     * 获取资源类型名称
     */
    private String getTypeName(ResourceType type) {
        switch (type) {
            case MENU:
                return "菜单";
            case BUTTON:
                return "按钮";
            case API:
                return "API";
            default:
                return "未知";
        }
    }
} 