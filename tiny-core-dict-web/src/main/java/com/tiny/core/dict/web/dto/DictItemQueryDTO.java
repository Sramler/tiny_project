package com.tiny.core.dict.web.dto;

/**
 * 字典项查询 DTO
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public class DictItemQueryDTO {
    private String dictCode;
    private String value;
    private Boolean enabled;

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}

