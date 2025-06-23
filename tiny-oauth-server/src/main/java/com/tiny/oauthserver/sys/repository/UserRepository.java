package com.tiny.oauthserver.sys.repository;

import com.tiny.oauthserver.sys.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    @EntityGraph(attributePaths = {"roles", "roles.resources"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<User> findUserByUsername(String username);
}