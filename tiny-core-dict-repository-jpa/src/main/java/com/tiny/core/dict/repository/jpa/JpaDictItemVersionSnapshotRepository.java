package com.tiny.core.dict.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 字典项版本快照 Repository
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@Repository
public interface JpaDictItemVersionSnapshotRepository extends JpaRepository<JpaDictItemVersionSnapshot, Long> {
    
    List<JpaDictItemVersionSnapshot> findByDictVersionId(Long dictVersionId);
}

