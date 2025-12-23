package com.tiny.core.dict.repository.jpa;

import com.tiny.core.dict.model.DictType;
import com.tiny.core.dict.repository.DictTypeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 字典类型 Repository JPA 实现
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@Component
public class JpaDictTypeRepositoryImpl implements DictTypeRepository {
    
    private final JpaDictTypeRepository jpaRepository;
    
    public JpaDictTypeRepositoryImpl(JpaDictTypeRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Optional<DictType> findByDictCode(String dictCode) {
        return jpaRepository.findByDictCode(dictCode)
            .map(DictRepositoryConverter::toCoreDictType);
    }
    
    @Override
    public Optional<DictType> findByDictCodeAndTenantId(String dictCode, Long tenantId) {
        return jpaRepository.findByDictCodeAndTenantId(dictCode, tenantId)
            .map(DictRepositoryConverter::toCoreDictType);
    }
    
    @Override
    public List<DictType> findByTenantIdIn(List<Long> tenantIds) {
        return jpaRepository.findByTenantIdIn(tenantIds).stream()
            .map(DictRepositoryConverter::toCoreDictType)
            .collect(Collectors.toList());
    }
    
    @Override
    public DictType save(DictType dictType) {
        JpaDictType jpa = DictRepositoryConverter.toJpaDictType(dictType);
        JpaDictType saved = jpaRepository.save(jpa);
        return DictRepositoryConverter.toCoreDictType(saved);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public Optional<DictType> findById(Long id) {
        return jpaRepository.findById(id)
            .map(DictRepositoryConverter::toCoreDictType);
    }
}

