package com.tiny.core.dict.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量获取字典标签请求 DTO
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public class BatchLabelRequest {
    @NotBlank(message = "字典编码不能为空")
    private String dictCode;

    @NotEmpty(message = "字典值列表不能为空")
    private List<String> values;

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}

