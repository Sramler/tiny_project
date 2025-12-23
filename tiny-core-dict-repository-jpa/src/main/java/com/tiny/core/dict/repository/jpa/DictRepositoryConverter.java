package com.tiny.core.dict.repository.jpa;

import com.tiny.core.dict.model.DictItem;
import com.tiny.core.dict.model.DictType;

/**
 * Repository 转换工具类
 * 
 * <p>用于在 JPA 实体和 Core 实体之间转换。
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public class DictRepositoryConverter {
    
    /**
     * JPA DictType 转换为 Core DictType
     */
    public static DictType toCoreDictType(JpaDictType jpa) {
        if (jpa == null) {
            return null;
        }
        DictType core = new DictType();
        core.setId(jpa.getId());
        core.setDictCode(jpa.getDictCode());
        core.setDictName(jpa.getDictName());
        core.setDescription(jpa.getDescription());
        core.setTenantId(jpa.getTenantId());
        core.setCategoryId(jpa.getCategoryId());
        core.setEnabled(jpa.getEnabled());
        core.setSortOrder(jpa.getSortOrder());
        core.setExtAttrs(jpa.getExtAttrs());
        core.setCreatedAt(jpa.getCreatedAt());
        core.setUpdatedAt(jpa.getUpdatedAt());
        core.setCreatedBy(jpa.getCreatedBy());
        core.setUpdatedBy(jpa.getUpdatedBy());
        return core;
    }
    
    /**
     * Core DictType 转换为 JPA DictType
     */
    public static JpaDictType toJpaDictType(DictType core) {
        if (core == null) {
            return null;
        }
        JpaDictType jpa = new JpaDictType();
        jpa.setId(core.getId());
        jpa.setDictCode(core.getDictCode());
        jpa.setDictName(core.getDictName());
        jpa.setDescription(core.getDescription());
        jpa.setTenantId(core.getTenantId());
        jpa.setCategoryId(core.getCategoryId());
        jpa.setEnabled(core.getEnabled());
        jpa.setSortOrder(core.getSortOrder());
        jpa.setExtAttrs(core.getExtAttrs());
        jpa.setCreatedAt(core.getCreatedAt());
        jpa.setUpdatedAt(core.getUpdatedAt());
        jpa.setCreatedBy(core.getCreatedBy());
        jpa.setUpdatedBy(core.getUpdatedBy());
        return jpa;
    }
    
    /**
     * JPA DictItem 转换为 Core DictItem
     */
    public static DictItem toCoreDictItem(JpaDictItem jpa) {
        if (jpa == null) {
            return null;
        }
        DictItem core = new DictItem();
        core.setId(jpa.getId());
        core.setDictTypeId(jpa.getDictTypeId());
        core.setValue(jpa.getValue());
        core.setLabel(jpa.getLabel());
        core.setDescription(jpa.getDescription());
        core.setTenantId(jpa.getTenantId());
        core.setEnabled(jpa.getEnabled());
        core.setSortOrder(jpa.getSortOrder());
        core.setExtAttrs(jpa.getExtAttrs());
        core.setCreatedAt(jpa.getCreatedAt());
        core.setUpdatedAt(jpa.getUpdatedAt());
        core.setCreatedBy(jpa.getCreatedBy());
        core.setUpdatedBy(jpa.getUpdatedBy());
        return core;
    }
    
    /**
     * Core DictItem 转换为 JPA DictItem
     */
    public static JpaDictItem toJpaDictItem(DictItem core) {
        if (core == null) {
            return null;
        }
        JpaDictItem jpa = new JpaDictItem();
        jpa.setId(core.getId());
        jpa.setDictTypeId(core.getDictTypeId());
        jpa.setValue(core.getValue());
        jpa.setLabel(core.getLabel());
        jpa.setDescription(core.getDescription());
        jpa.setTenantId(core.getTenantId());
        jpa.setEnabled(core.getEnabled());
        jpa.setSortOrder(core.getSortOrder());
        jpa.setExtAttrs(core.getExtAttrs());
        jpa.setCreatedAt(core.getCreatedAt());
        jpa.setUpdatedAt(core.getUpdatedAt());
        jpa.setCreatedBy(core.getCreatedBy());
        jpa.setUpdatedBy(core.getUpdatedBy());
        return jpa;
    }
}

