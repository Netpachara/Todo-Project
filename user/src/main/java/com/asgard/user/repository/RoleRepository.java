package com.asgard.user.repository;

import com.asgard.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    @Query("FROM Role r "+ "WHERE r.roleID = :id")
    Role findRoleID(@Param("id") Integer id);

    @Query("FROM Role r "+ "WHERE r.roleID in :roleIDList")
    List<Role> findRole(@Param("roleIDList") List<Integer> roleIDList);

}
