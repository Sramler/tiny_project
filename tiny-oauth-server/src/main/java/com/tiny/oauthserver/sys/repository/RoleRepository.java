package com.tiny.oauthserver.sys.repository;

import com.tiny.oauthserver.sys.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    @Query("select r from Role r left join fetch r.users where r.id = :id")
    Optional<Role> findByIdFetchUsers(@Param("id") Long id);

}