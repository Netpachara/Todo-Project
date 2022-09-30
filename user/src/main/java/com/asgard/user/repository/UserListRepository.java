package com.asgard.user.repository;

import com.asgard.user.payload.request.RequestUserList;
import com.asgard.user.payload.response.ResponseUserList;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class UserListRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<ResponseUserList> getUserList(RequestUserList req){
        String text = "SELECT r.RoleID as ID, u.*, r.RoleID as roleID, r.Title as title FROM [User] u INNER JOIN User_Role ur ON u.userID = ur.userID INNER JOIN Role r ON ur.roleID = r.roleID ";
        String condition = "" ;
        Integer page = req.getPageSize() * (req.getPage()-1);
        if(req.getSearch() != null){
            condition += "WHERE (STR(u.userID) LIKE '%" + req.getSearch() + "%' OR LOWER(u.fullName) LIKE '%" + req.getSearch().toLowerCase() + "%' OR LOWER(u.email) LIKE '%" + req.getSearch().toLowerCase() + "%') ";
        }

        if(!req.getRoleID().isEmpty()){
            condition +=  checkCondition(condition);
            condition += "r.roleID IN :roleList " ;
        }
        text += condition;

        System.out.println(text);

        Query typedQuery = null ;

        if(req.getRoleID().isEmpty()){
            typedQuery = entityManager.createNativeQuery(text, ResponseUserList.class) ;
        }
        else{
            typedQuery = entityManager.createNativeQuery(text, ResponseUserList.class)
                    .setParameter("roleList", req.getRoleID());
        }

        return typedQuery.setFirstResult(page).setMaxResults(req.getPageSize()).getResultList();
    }

    private String checkCondition(String condition){
        return condition.equalsIgnoreCase("") ? "WHERE " : "AND ";
    }
}
