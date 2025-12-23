package com.tiny.core.dict.repository.jpa;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 字典项版本快照 JPA 实体类
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@Entity
@Table(name = "dict_item_version_snapshot")
public class JpaDictItemVersionSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dict_version_id", nullable = false)
    private Long dictVersionId;

    @Column(name = "dict_item_id")
    private Long dictItemId;

    @Column(name = "value", nullable = false, length = 64)
    private String value;

    @Column(name = "label", nullable = false, length = 128)
    private String label;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "enabled")
    private Boolean enabled = true;

    @Column(name = "ext_attrs", columnDefinition = "JSON")
    private String extAttrs;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDictVersionId() {
        return dictVersionId;
    }

    public void setDictVersionId(Long dictVersionId) {
        this.dictVersionId = dictVersionId;
    }

    public Long getDictItemId() {
        return dictItemId;
    }

    public void setDictItemId(Long dictItemId) {
        this.dictItemId = dictItemId;
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

    public String getExtAttrs() {
        return extAttrs;
    }

    public void setExtAttrs(String extAttrs) {
        this.extAttrs = extAttrs;
    }
}

