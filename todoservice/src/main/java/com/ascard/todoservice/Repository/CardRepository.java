package com.ascard.todoservice.Repository;

import com.ascard.todoservice.Entity.Card;
import com.ascard.todoservice.payload.response.ResponseCardDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Integer> {

    Card findByCardID(@Param("id") Integer cardID);

    @Query("FROM Card c WHERE c.cardID in :cardIDList")
    List<Card> findCardDetails(@Param("cardIDList") List<Integer> cardID);

    @Query("FROM Card c WHERE c.cardID LIKE %:search% OR c.cardName LIKE %:search%")
    List<Card> findBySearch(@Param("search") String search);

    @Query("FROM Card c LEFT JOIN c.userCard u WHERE c.cardID = u.userCardID.cardID")
    List<Card> findList();


}
