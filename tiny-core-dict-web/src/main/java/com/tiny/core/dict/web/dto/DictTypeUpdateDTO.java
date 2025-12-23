package com.tiny.core.dict.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 字典类型更新 DTO
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public class DictTypeUpdateDTO {
    @NotBlank(message = "字典名称不能为空")
    private String dictName;

    private String description;
    private Long categoryId;
    private Boolean enabled;

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}

