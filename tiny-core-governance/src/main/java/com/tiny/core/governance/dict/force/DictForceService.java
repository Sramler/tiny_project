package com.tiny.core.governance.dict.force;

import com.tiny.core.dict.cache.DictCacheManager;
import com.tiny.core.dict.model.DictItem;
import com.tiny.core.dict.repository.DictItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Level2：FORCE 变更服务
 *
 * <p>仅治理模块可用，允许以受控方式强制修改字典项。
 */
@Service
public class DictForceService {

    private final DictItemRepository dictItemRepository;
    private final DictCacheManager dictCacheManager;

    public DictForceService(DictItemRepository dictItemRepository,
                            DictCacheManager dictCacheManager) {
        this.dictItemRepository = dictItemRepository;
        this.dictCacheManager = dictCacheManager;
    }

    /**
     * 强制更新字典标签（FORCE 变更）
     *
     * @param dictCode 字典编码
     * @param value    字典值
     * @param newLabel 新标签
     */
    @Transactional
    public void forceUpdateLabel(String dictCode, String value, String newLabel, Long tenantId) {
        // 这里简化实现：假设 Repository 已经支持通过 dictCode + value + tenantId 查询
        DictItem item = dictItemRepository
                .findByDictTypeIdAndValueAndTenantId(resolveDictTypeId(dictCode), value, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("字典项不存在"));

        item.setLabel(newLabel);
        dictItemRepository.save(item);

        // 异步刷新缓存
        dictCacheManager.refreshDictCacheAsync(dictCode, tenantId);
    }

    private Long resolveDictTypeId(String dictCode) {
        // TODO: 查询 DictType 获取 ID，这里简化为 0，占位实现
        return 0L;
    }
}
