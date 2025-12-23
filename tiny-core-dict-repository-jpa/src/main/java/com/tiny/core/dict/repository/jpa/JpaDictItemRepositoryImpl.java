package com.tiny.core.dict.repository.jpa;

import com.tiny.core.dict.model.DictItem;
import com.tiny.core.dict.repository.DictItemRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 字典项 Repository JPA 实现
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@Component
public class JpaDictItemRepositoryImpl implements DictItemRepository {
    
    private final JpaDictItemRepository jpaRepository;
    
    public JpaDictItemRepositoryImpl(JpaDictItemRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public List<DictItem> findByDictTypeIdAndTenantIdInOrderBySortOrder(Long dictTypeId, List<Long> tenantIds) {
        return jpaRepository.findByDictTypeIdAndTenantIdInOrderBySortOrder(dictTypeId, tenantIds).stream()
            .map(DictRepositoryConverter::toCoreDictItem)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<DictItem> findByDictTypeId(Long dictTypeId) {
        return jpaRepository.findByDictTypeId(dictTypeId).stream()
            .map(DictRepositoryConverter::toCoreDictItem)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<DictItem> findByDictTypeIdAndTenantId(Long dictTypeId, Long tenantId) {
        return jpaRepository.findByDictTypeIdAndTenantId(dictTypeId, tenantId).stream()
            .map(DictRepositoryConverter::toCoreDictItem)
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<DictItem> findByDictTypeIdAndValueAndTenantId(Long dictTypeId, String value, Long tenantId) {
        return jpaRepository.findByDictTypeIdAndValueAndTenantId(dictTypeId, value, tenantId)
            .map(DictRepositoryConverter::toCoreDictItem);
    }
    
    @Override
    public DictItem save(DictItem dictItem) {
        JpaDictItem jpa = DictRepositoryConverter.toJpaDictItem(dictItem);
        JpaDictItem saved = jpaRepository.save(jpa);
        return DictRepositoryConverter.toCoreDictItem(saved);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public Optional<DictItem> findById(Long id) {
        return jpaRepository.findById(id)
            .map(DictRepositoryConverter::toCoreDictItem);
    }
}

