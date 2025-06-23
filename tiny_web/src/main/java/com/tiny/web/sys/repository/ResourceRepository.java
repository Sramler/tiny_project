package com.tiny.web.sys.repository;

import com.tiny.web.sys.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    @Query("SELECT r FROM Resource r JOIN r.roles role WHERE role.name = :roleName")
    List<Resource> findByRoleName(@Param("roleName") String roleName);
}