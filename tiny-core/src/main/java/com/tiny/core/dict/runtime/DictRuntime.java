package com.tiny.core.dict.runtime;

import java.util.Map;

/**
 * 字典运行时 API 接口
 * 
 * <p>核心 API，提供字典查询能力。
 * 具体实现将在 Repository 模块中提供。
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public interface DictRuntime {
    /**
     * 获取字典标签
     * 
     * <p>根据字典编码、字典值和租户ID获取对应的标签。
     * 优先返回租户自定义的标签，如果不存在则返回平台字典的标签。
     * 
     * @param dictCode 字典编码，如 "GENDER", "ORDER_STATUS"
     * @param value 字典值，如 "MALE", "FEMALE", "PENDING", "PAID"
     * @param tenantId 租户ID
     * @return 字典标签，如 "男", "女", "待支付", "已支付"
     */
    String getLabel(String dictCode, String value, Long tenantId);

    /**
     * 获取字典所有项（value → label 映射）
     * 
     * <p>返回平台字典和租户字典合并后的结果，租户值覆盖平台值。
     * 
     * @param dictCode 字典编码
     * @param tenantId 租户ID
     * @return value → label 映射表
     */
    Map<String, String> getDict(String dictCode, Long tenantId);

    /**
     * 批量获取字典标签
     * 
     * <p>根据字典编码、字典值列表和租户ID批量获取对应的标签。
     * 
     * @param dictCode 字典编码
     * @param values 字典值列表
     * @param tenantId 租户ID
     * @return value → label 映射表
     */
    Map<String, String> getLabels(String dictCode, java.util.List<String> values, Long tenantId);

    /**
     * 刷新字典缓存
     * 
     * @param dictCode 字典编码
     * @param tenantId 租户ID
     */
    void refreshCache(String dictCode, Long tenantId);
}

