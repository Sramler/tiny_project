package com.tiny.core.dict.web.converter;

import com.tiny.core.dict.model.DictType;
import com.tiny.core.dict.web.dto.DictTypeCreateDTO;
import com.tiny.core.dict.web.dto.DictTypeDTO;
import com.tiny.core.dict.web.dto.DictTypeUpdateDTO;

import java.time.LocalDateTime;

/**
 * 字典类型转换器
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public class DictTypeConverter {
    
    /**
     * Entity 转 DTO
     */
    public static DictTypeDTO toDTO(DictType entity) {
        if (entity == null) {
            return null;
        }
        DictTypeDTO dto = new DictTypeDTO();
        dto.setId(entity.getId());
        dto.setDictCode(entity.getDictCode());
        dto.setDictName(entity.getDictName());
        dto.setDescription(entity.getDescription());
        dto.setTenantId(entity.getTenantId());
        dto.setCategoryId(entity.getCategoryId());
        dto.setEnabled(entity.getEnabled());
        dto.setSortOrder(entity.getSortOrder());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
    
    /**
     * CreateDTO 转 Entity
     */
    public static DictType toEntity(DictTypeCreateDTO dto, Long tenantId) {
        if (dto == null) {
            return null;
        }
        DictType entity = new DictType();
        entity.setDictCode(dto.getDictCode());
        entity.setDictName(dto.getDictName());
        entity.setDescription(dto.getDescription());
        entity.setCategoryId(dto.getCategoryId());
        entity.setTenantId(tenantId);
        entity.setEnabled(true);
        entity.setSortOrder(0);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
    
    /**
     * UpdateDTO 更新 Entity
     */
    public static void updateEntity(DictType entity, DictTypeUpdateDTO dto) {
        if (entity == null || dto == null) {
            return;
        }
        if (dto.getDictName() != null) {
            entity.setDictName(dto.getDictName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getCategoryId() != null) {
            entity.setCategoryId(dto.getCategoryId());
        }
        if (dto.getEnabled() != null) {
            entity.setEnabled(dto.getEnabled());
        }
        entity.setUpdatedAt(LocalDateTime.now());
    }
}

