package com.tiny.core.dict.runtime;

import com.tiny.core.dict.cache.DictCache;
import com.tiny.core.dict.cache.DictCacheManager;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字典运行时 API 实现
 * 
 * <p>提供给业务代码使用的统一入口。
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@Service
public class DictRuntimeImpl implements DictRuntime {
    
    private final DictCacheManager dictCacheManager;
    
    public DictRuntimeImpl(DictCacheManager dictCacheManager) {
        this.dictCacheManager = dictCacheManager;
    }
    
    @Override
    public String getLabel(String dictCode, String value, Long tenantId) {
        DictCache cache = dictCacheManager.getDictCache(dictCode, tenantId);
        return cache.getValueLabelMap().getOrDefault(value, "");
    }
    
    @Override
    public Map<String, String> getDict(String dictCode, Long tenantId) {
        DictCache cache = dictCacheManager.getDictCache(dictCode, tenantId);
        return new HashMap<>(cache.getValueLabelMap());
    }
    
    @Override
    public Map<String, String> getLabels(String dictCode, List<String> values, Long tenantId) {
        DictCache cache = dictCacheManager.getDictCache(dictCode, tenantId);
        Map<String, String> result = new HashMap<>();
        Map<String, String> valueLabelMap = cache.getValueLabelMap();
        for (String value : values) {
            result.put(value, valueLabelMap.getOrDefault(value, ""));
        }
        return result;
    }
    
    @Override
    public void refreshCache(String dictCode, Long tenantId) {
        dictCacheManager.refreshDictCache(dictCode, tenantId);
    }
}

