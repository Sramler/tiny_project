package com.tiny.core.dict.starter.initializer;

import com.tiny.core.dict.model.DictItem;
import com.tiny.core.dict.model.DictType;
import com.tiny.core.dict.repository.DictItemRepository;
import com.tiny.core.dict.repository.DictTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 字典初始化器
 * 
 * <p>应用启动时自动初始化平台字典（tenantId = 0）。
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@Component
public class DictInitializer implements ApplicationListener<ApplicationReadyEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(DictInitializer.class);
    
    private final DictTypeRepository dictTypeRepository;
    private final DictItemRepository dictItemRepository;
    
    public DictInitializer(DictTypeRepository dictTypeRepository,
                           DictItemRepository dictItemRepository) {
        this.dictTypeRepository = dictTypeRepository;
        this.dictItemRepository = dictItemRepository;
    }
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("开始初始化平台字典...");
        try {
            initializePlatformDicts();
            logger.info("平台字典初始化完成");
        } catch (Exception e) {
            logger.error("平台字典初始化失败", e);
        }
    }
    
    /**
     * 初始化平台字典
     */
    private void initializePlatformDicts() {
        // 初始化性别字典
        DictType genderType = createDictTypeIfNotExists("GENDER", "性别", "性别字典", 0L);
        createDictItemIfNotExists(genderType.getId(), "MALE", "男", "男性", 1, 0L);
        createDictItemIfNotExists(genderType.getId(), "FEMALE", "女", "女性", 2, 0L);
        
        // 初始化订单状态字典
        DictType orderStatusType = createDictTypeIfNotExists("ORDER_STATUS", "订单状态", "订单状态字典", 0L);
        createDictItemIfNotExists(orderStatusType.getId(), "PENDING", "待支付", "订单待支付", 1, 0L);
        createDictItemIfNotExists(orderStatusType.getId(), "PAID", "已支付", "订单已支付", 2, 0L);
        createDictItemIfNotExists(orderStatusType.getId(), "SHIPPED", "已发货", "订单已发货", 3, 0L);
        createDictItemIfNotExists(orderStatusType.getId(), "COMPLETED", "已完成", "订单已完成", 4, 0L);
        createDictItemIfNotExists(orderStatusType.getId(), "CANCELLED", "已取消", "订单已取消", 5, 0L);
    }
    
    /**
     * 创建字典类型（如果不存在）
     */
    private DictType createDictTypeIfNotExists(String dictCode, String dictName, String description, Long tenantId) {
        Optional<DictType> existing = dictTypeRepository.findByDictCodeAndTenantId(dictCode, tenantId);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        DictType dictType = new DictType();
        dictType.setDictCode(dictCode);
        dictType.setDictName(dictName);
        dictType.setDescription(description);
        dictType.setTenantId(tenantId);
        dictType.setEnabled(true);
        dictType.setSortOrder(0);
        dictType.setCreatedAt(LocalDateTime.now());
        dictType.setUpdatedAt(LocalDateTime.now());
        
        return dictTypeRepository.save(dictType);
    }
    
    /**
     * 创建字典项（如果不存在）
     */
    private DictItem createDictItemIfNotExists(Long dictTypeId, String value, String label, 
                                               String description, Integer sortOrder, Long tenantId) {
        Optional<DictItem> existing = dictItemRepository.findByDictTypeIdAndValueAndTenantId(
            dictTypeId, value, tenantId);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        DictItem dictItem = new DictItem();
        dictItem.setDictTypeId(dictTypeId);
        dictItem.setValue(value);
        dictItem.setLabel(label);
        dictItem.setDescription(description);
        dictItem.setTenantId(tenantId);
        dictItem.setEnabled(true);
        dictItem.setSortOrder(sortOrder);
        dictItem.setCreatedAt(LocalDateTime.now());
        dictItem.setUpdatedAt(LocalDateTime.now());
        
        return dictItemRepository.save(dictItem);
    }
}

