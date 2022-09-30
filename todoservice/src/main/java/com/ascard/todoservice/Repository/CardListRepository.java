package com.ascard.todoservice.Repository;


import com.ascard.todoservice.payload.request.RequestCardList;
import com.ascard.todoservice.payload.response.ResponseUserCardList;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.util.List;

@Repository
public class CardListRepository {

    @PersistenceContext
    private EntityManager entityManager;


    public List<ResponseUserCardList> findCardList(RequestCardList req){
        String text = "SELECT c.*, u.UserID as UserAssignedID, u.CardID as CardAssignedID FROM Card c LEFT JOIN User_Card u ON c.cardID = u.cardID ";
        String condition = "" ;
        Integer page = req.getPageSize() * (req.getPage()-1);

        if(req.getSearch() != null){
            condition += "WHERE (STR(c.cardID) LIKE '%" + req.getSearch().toLowerCase() + "%' OR LOWER(c.cardName) LIKE '%" + req.getSearch().toLowerCase() + "%') ";
        }
        if(req.getCardStatus() != null){
            condition +=  checkCondition(condition);
            condition += "c.status = '" + req.getCardStatus() + "' ";
        }
        if(req.getCreatedAtFrom() != null && req.getCreatedAtTo() != null){
            condition +=  checkCondition(condition);
            condition += "(c.createAt BETWEEN :startDate AND :endDate) ";
        }
        if(req.getCreatedByUserID() != null){
            condition +=  checkCondition(condition);
            condition += "c.createByUserID = " + req.getCreatedByUserID() + " ";
        }
        if(req.getAssigned()){
            condition +=  checkCondition(condition);
            condition += "u.userID = " + req.getAssignedUserID() + " " ;
        }
        if(!req.getAssigned()){
            condition +=  checkCondition(condition);
            condition += "u.userID IS NULL " ;
        }
        if(req.getSortBy() != null){
            condition += "ORDER BY " + req.getSortBy() + " ";
        }
        if(req.getSortDirection() != null){
            condition += req.getSortDirection() + " ";
        }
        text += condition ;

        System.out.println(text);
        Query typedQuery = entityManager.createNativeQuery(text, ResponseUserCardList.class)
                .setParameter("startDate", req.getCreatedAtFrom(), TemporalType.DATE)
                .setParameter("endDate", req.getCreatedAtTo(), TemporalType.DATE);
        return typedQuery.setFirstResult(page).setMaxResults(req.getPageSize()).getResultList();
    }

    private String checkCondition(String condition){
        return condition.equalsIgnoreCase("") ? "WHERE " : "AND ";
    }
}
