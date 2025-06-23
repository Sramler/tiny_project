package com.tiny.oauthserver.sys.model;

import com.tiny.oauthserver.sys.enums.ResourceType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false) // 建议只手动标注使用
public class ResourceTypeConverter implements AttributeConverter<ResourceType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ResourceType attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public ResourceType convertToEntityAttribute(Integer dbData) {
        return ResourceType.fromCode(dbData);
    }
}