package com.asgard.user.repository;


import com.asgard.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInformationRepository extends JpaRepository<User, Integer> {

//    @Query("");

}
