package com.asgard.user.repository;


import com.asgard.user.entity.Role;
import com.asgard.user.entity.User;
import com.asgard.user.entity.User_Role;
import com.asgard.user.payload.response.ResponseUserDetailsAndRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    User findByEmail(String email);

    @Query("FROM User u WHERE u.email = :email AND u.userID != :id")
    User findByEmailANDUserID(@Param("email") String email, @Param("id") Integer id);

    @Query("FROM User u " + "WHERE u.email = :email AND u.password = :password")
    User checkLogin(@Param("email") String email, @Param("password") String password);

    @Query("FROM User u " + "INNER JOIN u.user_role ur INNER JOIN ur.role r " +
            "WHERE (STR(u.userID) LIKE %:search% OR LOWER(u.fullName) LIKE %:search% OR LOWER(u.email) LIKE %:search%)" +
            " AND r.roleID = :roleID")
    List<User> getUserList(@Param("search") String search, @Param("roleID") Integer roleID);

    User findByUserID(@Param("id") Integer id);

}
