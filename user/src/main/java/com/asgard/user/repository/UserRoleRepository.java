package com.asgard.user.repository;

import com.asgard.user.entity.User_Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<User_Role, Integer> {

    @Query("FROM User_Role u WHERE u.userRoleId.userID = :id")
    User_Role findByUserID(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("DELETE FROM User_Role u WHERE u.userRoleId.userID = :id")
    void deleteByUserID(@Param("id") Integer id);

    @Query("FROM User_Role r " + "WHERE r.userRoleId.userID = :id")
    List<User_Role> getUserDetails(@Param("id") Integer id);

}
