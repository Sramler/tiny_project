package com.tiny.core.dict.web.dto;

/**
 * 字典类型查询 DTO
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public class DictTypeQueryDTO {
    private String dictCode;
    private String dictName;
    private Long categoryId;

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}

