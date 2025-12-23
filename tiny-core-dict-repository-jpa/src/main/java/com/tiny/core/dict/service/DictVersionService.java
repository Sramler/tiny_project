package com.tiny.core.dict.service;

import com.tiny.core.dict.model.DictItem;
import com.tiny.core.dict.model.DictType;
import com.tiny.core.dict.repository.DictItemRepository;
import com.tiny.core.dict.repository.DictTypeRepository;
import com.tiny.core.dict.repository.jpa.JpaDictItemVersionSnapshot;
import com.tiny.core.dict.repository.jpa.JpaDictItemVersionSnapshotRepository;
import com.tiny.core.dict.repository.jpa.JpaDictVersion;
import com.tiny.core.dict.repository.jpa.JpaDictVersionRepository;
import com.tiny.core.dict.runtime.DictRuntime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典版本管理服务
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@Service
public class DictVersionService {
    
    private final DictTypeRepository dictTypeRepository;
    private final DictItemRepository dictItemRepository;
    private final JpaDictVersionRepository versionRepository;
    private final JpaDictItemVersionSnapshotRepository snapshotRepository;
    private final DictRuntime dictRuntime;
    
    public DictVersionService(DictTypeRepository dictTypeRepository,
                              DictItemRepository dictItemRepository,
                              JpaDictVersionRepository versionRepository,
                              JpaDictItemVersionSnapshotRepository snapshotRepository,
                              DictRuntime dictRuntime) {
        this.dictTypeRepository = dictTypeRepository;
        this.dictItemRepository = dictItemRepository;
        this.versionRepository = versionRepository;
        this.snapshotRepository = snapshotRepository;
        this.dictRuntime = dictRuntime;
    }
    
    /**
     * 创建字典版本快照
     */
    @Transactional
    public JpaDictVersion createVersion(String dictCode, Long tenantId, String description, String createdBy) {
        // 1. 查找字典类型
        DictType dictType = dictTypeRepository.findByDictCode(dictCode)
            .orElseThrow(() -> new IllegalArgumentException("字典类型不存在: " + dictCode));
        
        // 2. 获取当前字典项
        List<DictItem> items = dictItemRepository.findByDictTypeIdAndTenantIdInOrderBySortOrder(
            dictType.getId(), List.of(0L, tenantId));
        
        // 3. 生成版本号
        String version = getNextVersion(dictCode, tenantId);
        
        // 4. 创建版本记录
        JpaDictVersion versionEntity = new JpaDictVersion();
        versionEntity.setDictCode(dictCode);
        versionEntity.setVersion(version);
        versionEntity.setTenantId(tenantId);
        versionEntity.setDescription(description);
        versionEntity.setCreatedBy(createdBy);
        versionEntity.setCreatedAt(LocalDateTime.now());
        versionEntity = versionRepository.save(versionEntity);
        
        // 5. 创建快照
        for (DictItem item : items) {
            JpaDictItemVersionSnapshot snapshot = new JpaDictItemVersionSnapshot();
            snapshot.setDictVersionId(versionEntity.getId());
            snapshot.setDictItemId(item.getId());
            snapshot.setValue(item.getValue());
            snapshot.setLabel(item.getLabel());
            snapshot.setSortOrder(item.getSortOrder());
            snapshot.setEnabled(item.getEnabled());
            snapshot.setExtAttrs(item.getExtAttrs());
            snapshotRepository.save(snapshot);
        }
        
        return versionEntity;
    }
    
    /**
     * 回滚到指定版本
     */
    @Transactional
    public void rollbackToVersion(String dictCode, String version, Long tenantId) {
        // 1. 获取版本快照
        JpaDictVersion dictVersion = versionRepository
            .findByDictCodeAndVersionAndTenantId(dictCode, version, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("版本不存在"));
        
        List<JpaDictItemVersionSnapshot> snapshots = snapshotRepository
            .findByDictVersionId(dictVersion.getId());
        
        // 2. 恢复字典项
        for (JpaDictItemVersionSnapshot snapshot : snapshots) {
            DictItem item = dictItemRepository.findById(snapshot.getDictItemId())
                .orElse(new DictItem());
            
            item.setValue(snapshot.getValue());
            item.setLabel(snapshot.getLabel());
            item.setSortOrder(snapshot.getSortOrder());
            item.setEnabled(snapshot.getEnabled());
            item.setExtAttrs(snapshot.getExtAttrs());
            dictItemRepository.save(item);
        }
        
        // 3. 刷新缓存
        dictRuntime.refreshCache(dictCode, tenantId);
    }
    
    /**
     * 获取下一个版本号
     */
    private String getNextVersion(String dictCode, Long tenantId) {
        List<JpaDictVersion> versions = versionRepository
            .findByDictCodeAndTenantIdOrderByCreatedAtDesc(dictCode, tenantId);
        
        if (versions.isEmpty()) {
            return "1.0.0";
        }
        
        // 简单版本号递增（实际可以使用语义化版本号）
        String lastVersion = versions.get(0).getVersion();
        // TODO: 实现版本号递增逻辑
        return incrementVersion(lastVersion);
    }
    
    private String incrementVersion(String version) {
        // 简化实现：假设版本号格式为 "1.0.0"
        String[] parts = version.split("\\.");
        int patch = Integer.parseInt(parts[2]) + 1;
        return parts[0] + "." + parts[1] + "." + patch;
    }
}

