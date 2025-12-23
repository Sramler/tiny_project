package com.tiny.core.dict.service;

import com.tiny.core.dict.model.DictType;
import com.tiny.core.dict.repository.DictTypeRepository;
import com.tiny.core.dict.web.dto.DictTypeCreateDTO;
import com.tiny.core.dict.web.dto.DictTypeDTO;
import com.tiny.core.dict.web.dto.DictTypeQueryDTO;
import com.tiny.core.dict.web.dto.DictTypeUpdateDTO;
import com.tiny.core.dict.web.converter.DictTypeConverter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典类型服务
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@Service
public class DictTypeService {
    
    private final DictTypeRepository dictTypeRepository;
    
    public DictTypeService(DictTypeRepository dictTypeRepository) {
        this.dictTypeRepository = dictTypeRepository;
    }
    
    /**
     * 分页查询字典类型
     */
    public Page<DictTypeDTO> listTypes(DictTypeQueryDTO query, Long tenantId, Pageable pageable) {
        // 简化实现：查询所有租户相关的字典类型（平台 + 租户）
        List<Long> tenantIds = List.of(0L, tenantId);
        List<DictType> types = dictTypeRepository.findByTenantIdIn(tenantIds);
        
        // 简单的内存过滤（实际应该使用 JPA Specification 或 QueryDSL）
        List<DictType> filtered = types.stream()
            .filter(type -> {
                if (query.getDictCode() != null && !type.getDictCode().contains(query.getDictCode())) {
                    return false;
                }
                if (query.getDictName() != null && !type.getDictName().contains(query.getDictName())) {
                    return false;
                }
                if (query.getCategoryId() != null && !query.getCategoryId().equals(type.getCategoryId())) {
                    return false;
                }
                return true;
            })
            .collect(Collectors.toList());
        
        // 转换为 DTO
        List<DictTypeDTO> dtos = filtered.stream()
            .map(DictTypeConverter::toDTO)
            .collect(Collectors.toList());
        
        // 简单的分页（实际应该使用数据库分页）
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<DictTypeDTO> pageContent = dtos.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, dtos.size());
    }
    
    /**
     * 创建字典类型
     */
    @Transactional
    public DictTypeDTO createType(DictTypeCreateDTO dto, Long tenantId) {
        // 检查字典编码是否已存在
        if (dictTypeRepository.findByDictCodeAndTenantId(dto.getDictCode(), tenantId).isPresent()) {
            throw new IllegalArgumentException("字典编码已存在: " + dto.getDictCode());
        }
        
        DictType entity = DictTypeConverter.toEntity(dto, tenantId);
        DictType saved = dictTypeRepository.save(entity);
        return DictTypeConverter.toDTO(saved);
    }
    
    /**
     * 更新字典类型
     */
    @Transactional
    public DictTypeDTO updateType(Long id, DictTypeUpdateDTO dto, Long tenantId) {
        DictType entity = dictTypeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("字典类型不存在: " + id));
        
        // 检查租户权限（租户只能修改自己的字典类型）
        if (!entity.getTenantId().equals(tenantId) && tenantId != 0L) {
            throw new IllegalArgumentException("无权修改该字典类型");
        }
        
        DictTypeConverter.updateEntity(entity, dto);
        DictType saved = dictTypeRepository.save(entity);
        return DictTypeConverter.toDTO(saved);
    }
    
    /**
     * 删除字典类型
     */
    @Transactional
    public void deleteType(Long id, Long tenantId) {
        DictType entity = dictTypeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("字典类型不存在: " + id));
        
        // 检查租户权限
        if (!entity.getTenantId().equals(tenantId) && tenantId != 0L) {
            throw new IllegalArgumentException("无权删除该字典类型");
        }
        
        dictTypeRepository.deleteById(id);
    }
}

