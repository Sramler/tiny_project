package com.tiny.oauthserver.sys.repository;

import com.tiny.oauthserver.sys.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {


    @EntityGraph(attributePaths = {"roles", "roles.resources"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<User> findUserByUsername(String username);

    /**
     * 重写findById方法，使用@EntityGraph注解，在查询用户时，立即加载roles集合，解决懒加载异常
     * @param id 用户ID
     * @return 包含角色的用户
     */
    @Override
    @EntityGraph(attributePaths = "roles")
    Optional<User> findById(Long id);

    @Query("select u from User u join u.roles r where r.id = :roleId")
    List<User> findByRoleId(@Param("roleId") Long roleId);
}