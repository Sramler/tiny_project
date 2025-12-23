package com.tiny.core.dict.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 字典项创建 DTO
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public class DictItemCreateDTO {
    @NotBlank(message = "字典编码不能为空")
    private String dictCode;

    @NotBlank(message = "字典值不能为空")
    @Pattern(regexp = "^[A-Z0-9_]{1,64}$", message = "字典值必须符合规范：大写字母、数字、下划线，1-64字符")
    private String value;

    @NotBlank(message = "字典标签不能为空")
    @Size(max = 128, message = "字典标签长度不能超过128字符")
    private String label;

    private String description;
    private Integer sortOrder = 0;
    private Boolean enabled = true;

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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}

