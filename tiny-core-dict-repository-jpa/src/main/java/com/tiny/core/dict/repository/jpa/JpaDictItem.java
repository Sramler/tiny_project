package com.tiny.core.dict.repository.jpa;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 字典项 JPA 实体类
 * 
 * <p>包含 JPA 注解，用于数据持久化。
 * 与 Core 模块的 DictItem 结构相同，但添加了 JPA 注解。
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@Entity
@Table(name = "dict_item", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_dict_type_value_tenant", 
                         columnNames = {"dict_type_id", "value", "tenant_id"})
    },
    indexes = {
        @Index(name = "idx_dict_type_id", columnList = "dict_type_id"),
        @Index(name = "idx_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_value", columnList = "value"),
        @Index(name = "idx_enabled", columnList = "enabled"),
        @Index(name = "idx_sort_order", columnList = "sort_order")
    })
public class JpaDictItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dict_type_id", nullable = false)
    private Long dictTypeId;

    @Column(name = "value", nullable = false, length = 64)
    private String value;

    @Column(name = "label", nullable = false, length = 128)
    private String label;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId = 0L;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "ext_attrs", columnDefinition = "JSON")
    private String extAttrs;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDictTypeId() {
        return dictTypeId;
    }

    public void setDictTypeId(Long dictTypeId) {
        this.dictTypeId = dictTypeId;
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

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getExtAttrs() {
        return extAttrs;
    }

    public void setExtAttrs(String extAttrs) {
        this.extAttrs = extAttrs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}

