package com.tiny.core.dict.repository.jpa;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 字典版本 JPA 实体类
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@Entity
@Table(name = "dict_version", uniqueConstraints = {
    @UniqueConstraint(name = "uk_dict_version_tenant", columnNames = {"dict_code", "version", "tenant_id"})
})
public class JpaDictVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dict_code", nullable = false, length = 64)
    private String dictCode;

    @Column(name = "version", nullable = false, length = 32)
    private String version;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId = 0L;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

