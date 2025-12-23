package com.tiny.core.dict.web.converter;

import com.tiny.core.dict.model.DictItem;
import com.tiny.core.dict.web.dto.DictItemCreateDTO;
import com.tiny.core.dict.web.dto.DictItemDTO;
import com.tiny.core.dict.web.dto.DictItemUpdateDTO;

import java.time.LocalDateTime;

/**
 * 字典项转换器
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public class DictItemConverter {
    
    /**
     * Entity 转 DTO
     */
    public static DictItemDTO toDTO(DictItem entity, String dictCode) {
        if (entity == null) {
            return null;
        }
        DictItemDTO dto = new DictItemDTO();
        dto.setId(entity.getId());
        dto.setDictTypeId(entity.getDictTypeId());
        dto.setDictCode(dictCode);
        dto.setValue(entity.getValue());
        dto.setLabel(entity.getLabel());
        dto.setDescription(entity.getDescription());
        dto.setTenantId(entity.getTenantId());
        dto.setEnabled(entity.getEnabled());
        dto.setSortOrder(entity.getSortOrder());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
    
    /**
     * CreateDTO 转 Entity
     */
    public static DictItem toEntity(DictItemCreateDTO dto, Long dictTypeId, Long tenantId) {
        if (dto == null) {
            return null;
        }
        DictItem entity = new DictItem();
        entity.setDictTypeId(dictTypeId);
        entity.setValue(dto.getValue());
        entity.setLabel(dto.getLabel());
        entity.setDescription(dto.getDescription());
        entity.setTenantId(tenantId);
        entity.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
    
    /**
     * UpdateDTO 更新 Entity
     */
    public static void updateEntity(DictItem entity, DictItemUpdateDTO dto) {
        if (entity == null || dto == null) {
            return;
        }
        if (dto.getLabel() != null) {
            entity.setLabel(dto.getLabel());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getSortOrder() != null) {
            entity.setSortOrder(dto.getSortOrder());
        }
        if (dto.getEnabled() != null) {
            entity.setEnabled(dto.getEnabled());
        }
        entity.setUpdatedAt(LocalDateTime.now());
    }
}

