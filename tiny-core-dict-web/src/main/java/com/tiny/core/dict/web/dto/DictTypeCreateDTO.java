package com.tiny.core.dict.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 字典类型创建 DTO
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public class DictTypeCreateDTO {
    @NotBlank(message = "字典编码不能为空")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]{2,63}$", message = "字典编码必须是大写字母开头，3-64字符")
    private String dictCode;

    @NotBlank(message = "字典名称不能为空")
    private String dictName;

    private String description;
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
}

