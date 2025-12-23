package com.tiny.core.dict.service;

import com.tiny.core.dict.model.DictItem;
import com.tiny.core.dict.model.DictType;
import com.tiny.core.dict.repository.DictItemRepository;
import com.tiny.core.dict.repository.DictTypeRepository;
import com.tiny.core.dict.web.dto.DictItemCreateDTO;
import com.tiny.core.dict.web.dto.DictItemDTO;
import com.tiny.core.dict.web.dto.DictItemQueryDTO;
import com.tiny.core.dict.web.dto.DictItemUpdateDTO;
import com.tiny.core.dict.web.converter.DictItemConverter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典项服务
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@Service
public class DictItemService {
    
    private final DictItemRepository dictItemRepository;
    private final DictTypeRepository dictTypeRepository;
    
    public DictItemService(DictItemRepository dictItemRepository, DictTypeRepository dictTypeRepository) {
        this.dictItemRepository = dictItemRepository;
        this.dictTypeRepository = dictTypeRepository;
    }
    
    /**
     * 分页查询字典项
     */
    public Page<DictItemDTO> listItems(DictItemQueryDTO query, Long tenantId, Pageable pageable) {
        // 根据字典编码查找字典类型
        DictType dictType = dictTypeRepository.findByDictCode(query.getDictCode())
            .orElseThrow(() -> new IllegalArgumentException("字典类型不存在: " + query.getDictCode()));
        
        // 查询字典项（平台 + 租户）
        List<Long> tenantIds = List.of(0L, tenantId);
        List<DictItem> items = dictItemRepository.findByDictTypeIdAndTenantIdInOrderBySortOrder(
            dictType.getId(), tenantIds);
        
        // 简单的内存过滤
        List<DictItem> filtered = items.stream()
            .filter(item -> {
                if (query.getValue() != null && !item.getValue().contains(query.getValue())) {
                    return false;
                }
                if (query.getEnabled() != null && !query.getEnabled().equals(item.getEnabled())) {
                    return false;
                }
                return true;
            })
            .collect(Collectors.toList());
        
        // 转换为 DTO
        List<DictItemDTO> dtos = filtered.stream()
            .map(item -> DictItemConverter.toDTO(item, query.getDictCode()))
            .collect(Collectors.toList());
        
        // 简单的分页
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<DictItemDTO> pageContent = dtos.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, dtos.size());
    }
    
    /**
     * 创建字典项
     */
    @Transactional
    public DictItemDTO createItem(DictItemCreateDTO dto, Long tenantId) {
        // 查找字典类型
        DictType dictType = dictTypeRepository.findByDictCode(dto.getDictCode())
            .orElseThrow(() -> new IllegalArgumentException("字典类型不存在: " + dto.getDictCode()));
        
        // 检查字典值是否已存在（同一租户内）
        if (dictItemRepository.findByDictTypeIdAndValueAndTenantId(
                dictType.getId(), dto.getValue(), tenantId).isPresent()) {
            throw new IllegalArgumentException("字典值已存在: " + dto.getValue());
        }
        
        DictItem entity = DictItemConverter.toEntity(dto, dictType.getId(), tenantId);
        DictItem saved = dictItemRepository.save(entity);
        return DictItemConverter.toDTO(saved, dto.getDictCode());
    }
    
    /**
     * 批量创建字典项
     */
    @Transactional
    public List<DictItemDTO> createItemsBatch(List<DictItemCreateDTO> dtos, Long tenantId) {
        return dtos.stream()
            .map(dto -> createItem(dto, tenantId))
            .collect(Collectors.toList());
    }
    
    /**
     * 更新字典项
     */
    @Transactional
    public DictItemDTO updateItem(Long id, DictItemUpdateDTO dto, Long tenantId) {
        DictItem entity = dictItemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("字典项不存在: " + id));
        
        // 检查租户权限（租户只能修改自己的字典项，或覆盖平台字典的 label）
        if (!entity.getTenantId().equals(tenantId) && tenantId != 0L) {
            // 如果是平台字典项，租户可以覆盖 label
            if (entity.getTenantId() == 0L) {
                // 创建租户覆盖项
                DictItem tenantItem = new DictItem();
                tenantItem.setDictTypeId(entity.getDictTypeId());
                tenantItem.setValue(entity.getValue());
                tenantItem.setLabel(dto.getLabel());
                tenantItem.setTenantId(tenantId);
                tenantItem.setEnabled(entity.getEnabled());
                tenantItem.setSortOrder(entity.getSortOrder());
                entity = dictItemRepository.save(tenantItem);
            } else {
                throw new IllegalArgumentException("无权修改该字典项");
            }
        } else {
            DictItemConverter.updateEntity(entity, dto);
            entity = dictItemRepository.save(entity);
        }
        
        // 获取字典编码
        DictType dictType = dictTypeRepository.findById(entity.getDictTypeId())
            .orElseThrow(() -> new IllegalArgumentException("字典类型不存在"));
        
        return DictItemConverter.toDTO(entity, dictType.getDictCode());
    }
    
    /**
     * 删除字典项
     */
    @Transactional
    public void deleteItem(Long id, Long tenantId) {
        DictItem entity = dictItemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("字典项不存在: " + id));
        
        // 检查租户权限
        if (!entity.getTenantId().equals(tenantId) && tenantId != 0L) {
            throw new IllegalArgumentException("无权删除该字典项");
        }
        
        dictItemRepository.deleteById(id);
    }
}

