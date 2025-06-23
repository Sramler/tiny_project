package com.tiny.oauthserver.sys.repository;

import com.tiny.oauthserver.sys.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

}