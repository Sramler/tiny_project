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
        String name = StringUtils.hasText(query.getName()) ? query.getName() : null;
        String path = StringUtils.hasText(query.getPath()) ? query.getPath() : null;
        String uri = StringUtils.hasText(query.getUri()) ? query.getUri() : null;
        String permission = StringUtils.hasText(query.getPermission()) ? query.getPermission() : null;
        String title = StringUtils.hasText(query.getTitle()) ? query.getTitle() : null;
        ResourceType type = query.getType() != null ? ResourceType.fromCode(query.getType()) : null;
        Long parentId = query.getParentId();
        Boolean hidden = query.getHidden();

        // 执行查询
        Page<Resource> resources = resourceRepository.findByConditions(
                name, path, uri, permission, title, type, parentId, hidden, pageable);

        // 转换为DTO
        return resources.map(this::toDto);
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
        existingResource.setPath(resource.getPath());
        existingResource.setUri(resource.getUri());
        existingResource.setMethod(resource.getMethod());
        existingResource.setIcon(resource.getIcon());
        existingResource.setShowIcon(resource.isShowIcon());
        existingResource.setSort(resource.getSort());
        existingResource.setComponent(resource.getComponent());
        existingResource.setRedirect(resource.getRedirect());
        existingResource.setHidden(resource.isHidden());
        existingResource.setKeepAlive(resource.isKeepAlive());
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
        
        // 检查路径是否已存在（如果提供了路径）
        if (StringUtils.hasText(resourceDto.getPath()) && 
            resourceRepository.findByPath(resourceDto.getPath()).isPresent()) {
            throw new RuntimeException("资源路径已存在");
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
        resource.setPath(resourceDto.getPath());
        resource.setUri(resourceDto.getUri());
        resource.setMethod(resourceDto.getMethod());
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
        
        // 检查路径是否已被其他资源使用（如果提供了路径）
        if (StringUtils.hasText(resourceDto.getPath())) {
            Optional<Resource> resourceWithSamePath = resourceRepository.findByPath(resourceDto.getPath());
            if (resourceWithSamePath.isPresent() && !resourceWithSamePath.get().getId().equals(resourceDto.getId())) {
                throw new RuntimeException("资源路径已被其他资源使用");
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
        existingResource.setPath(resourceDto.getPath());
        existingResource.setUri(resourceDto.getUri());
        existingResource.setMethod(resourceDto.getMethod());
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
    public Optional<Resource> findByPath(String path) {
        return resourceRepository.findByPath(path);
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
    public boolean existsByPath(String path, Long excludeId) {
        if (excludeId == null) {
            return resourceRepository.findByPath(path).isPresent();
        }
        return resourceRepository.existsByPathAndIdNot(path, excludeId);
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
        dto.setPath(resource.getPath());
        dto.setUri(resource.getUri());
        dto.setMethod(resource.getMethod());
        dto.setIcon(resource.getIcon());
        dto.setShowIcon(resource.isShowIcon());
        dto.setSort(resource.getSort());
        dto.setComponent(resource.getComponent());
        dto.setRedirect(resource.getRedirect());
        dto.setHidden(resource.isHidden());
        dto.setKeepAlive(resource.isKeepAlive());
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