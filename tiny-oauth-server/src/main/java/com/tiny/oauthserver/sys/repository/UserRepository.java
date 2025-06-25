package com.tiny.oauthserver.sys.repository;

import com.tiny.oauthserver.sys.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {


    @EntityGraph(attributePaths = {"roles", "roles.resources"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<User> findUserByUsername(String username);
}