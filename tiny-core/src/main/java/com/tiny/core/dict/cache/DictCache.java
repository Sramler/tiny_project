package com.tiny.core.dict.cache;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 字典缓存数据模型
 * 
 * <p>包含整个字典的 value→label 映射（包含平台+租户合并后的结果）。
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public class DictCache {
    /**
     * value → label 映射表
     * 包含平台字典和租户字典合并后的结果，租户值覆盖平台值
     */
    private Map<String, String> valueLabelMap;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;

    public DictCache() {
    }

    public DictCache(Map<String, String> valueLabelMap, LocalDateTime lastUpdateTime) {
        this.valueLabelMap = valueLabelMap;
        this.lastUpdateTime = lastUpdateTime;
    }

    public Map<String, String> getValueLabelMap() {
        return valueLabelMap;
    }

    public void setValueLabelMap(Map<String, String> valueLabelMap) {
        this.valueLabelMap = valueLabelMap;
    }

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}

