package com.asgard.user.repository;


import com.asgard.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

//    @Query("SELECT COUNT(*) FROM User u " + "WHERE u.email = :email")
//    boolean findDuplicateEmail(@Param("email") String email);

    User findByEmail(String email);

//    @Query("FROM User u " + "WHERE STR(u.userID) LIKE %:search% OR LOWER(u.fullName) LIKE %:search% OR LOWER(u.email) LIKE %:search%")
//    User findUserList(@Param("email") String search);

    @Query("FROM User u " + "WHERE u.email = :email AND u.password = :password")
    User checkLogin(@Param("email") String email, @Param("password") String password);

}
