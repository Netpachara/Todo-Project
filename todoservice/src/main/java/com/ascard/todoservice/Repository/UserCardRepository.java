package com.ascard.todoservice.Repository;

import com.ascard.todoservice.Entity.UserCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserCardRepository extends JpaRepository<UserCard, Integer> {

    @Query("FROM UserCard u WHERE u.userCardID.userID = :userid")
    List<UserCard> findByUserID(@Param("userid") Integer userid);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserCard u WHERE u.userCardID.userID =:userid")
    void deleteByUserID(@Param("userid") Integer userid);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserCard u WHERE u.userCardID.cardID =:cardId")
    void deleteByCardID(@Param("cardId") Integer cardId);

    @Query("FROM UserCard u WHERE u.userCardID.userID = :userId AND u.userCardID.cardID = :cardId")
    UserCard findByUserIDAndCardID(@Param("userId") Integer userid, @Param("cardId") Integer cardId);

    @Query("FROM UserCard u WHERE u.userCardID.cardID = :cardId")
    UserCard findByCardID(@Param("cardId") Integer cardId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserCard u WHERE u.userCardID.userID =:userId AND u.userCardID.cardID =:cardId")
    void deleteByUserIDAndCardID(@Param("userId") Integer userId, @Param("cardId") Integer cardId);
}
