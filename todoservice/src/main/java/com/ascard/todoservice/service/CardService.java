package com.ascard.todoservice.service;


import com.ascard.todoservice.Entity.Card;
import com.ascard.todoservice.Entity.UserCard;
import com.ascard.todoservice.Entity.embedded.UserCardID;
import com.ascard.todoservice.Repository.CardListRepository;
import com.ascard.todoservice.Repository.CardRepository;
import com.ascard.todoservice.Repository.UserCardRepository;
import com.ascard.todoservice.payload.request.RequestCardList;
import com.ascard.todoservice.payload.request.RequestEditCardDetails;
import com.ascard.todoservice.payload.request.RequestCreatedCard;
import com.ascard.todoservice.payload.request.RequestUpdateCardStatus;
import com.ascard.todoservice.payload.response.ResponseCardDetails;
import com.ascard.todoservice.payload.response.ResponseCardList;
import com.ascard.todoservice.payload.response.ResponseRole;
import com.ascard.todoservice.payload.response.ResponseUserCardDetails;
import com.ascard.todoservice.payload.response.ResponseUserCardList;
import com.ascard.todoservice.payload.response.ResponseUserDetails;
import com.ascard.todoservice.service.Client.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CardService {

    private CardRepository cardRepository;
    private UserCardRepository userCardRepository;
    private UserClient userClient;
    private CardListRepository cardListRepository;

    @Autowired
    public CardService(CardRepository c, UserCardRepository uc, UserClient userClient, CardListRepository cardList){
        this.cardRepository = c;
        this.userCardRepository = uc;
        this.userClient = userClient;
        this.cardListRepository = cardList;
    }

    @Transactional(rollbackFor = Exception.class )
    public Integer createdCard(RequestCreatedCard req) throws Exception {
        ResponseUserDetails resAssign = userClient.getUserDetails(req.getCreateByUserID());
        if(resAssign == null){
            throw new Exception("You do not have permission");
        }
        if(!IsAdmin(resAssign)){
            throw new Exception("This user is not admin");
        }
        Card card = new Card();
        Instant instant = Instant.now();
        card.setCardName(req.getCardName());
        card.setCardDetails(req.getCardDetails());
        card.setStatus("todo");
        card.setCreateAt(Date.from(instant));
        card.setCreateByUserID(resAssign.getUserID());
        cardRepository.save(card);

        if(req.getUserID() != null){
            ResponseUserDetails resUserAssigned = userClient.getUserDetails(req.getUserID());
            if(resUserAssigned != null){
                UserCard userCard = new UserCard();
                UserCardID userCardID = new UserCardID();
                userCardID.setUserID(req.getUserID());
                userCardID.setCardID(card.getCardID());
                userCard.setUserCardID(userCardID);
                userCardRepository.save(userCard);
            }
            else{
                throw new Exception("This userAssigned is not in database");
            }
        }
        return card.getCardID();
    }

    public Integer editCardDetails(RequestEditCardDetails req) throws Exception {
        ResponseUserDetails resAssign = userClient.getUserDetails(req.getUserID());

        if(resAssign == null){ //Don't have this userID in database
            throw new Exception("you do not have permission");
        }
        Instant instant = Instant.now();
        Card card = cardRepository.findByCardID(req.getCardID());
        if(card == null){
            throw new Exception("this card not in database");
        }
        card.setCardName(req.getCardName());
        card.setCardDetails(req.getCardDetails());
        card.setUpdateAt(Date.from(instant));
        cardRepository.save(card);

        if(IsAdmin(resAssign)){
            UserCard uCard = card.getUserCard(); // Hibernate get same cardID from card  Entity
            if(uCard == null){
                UserCard userCard = new UserCard();
                UserCardID userCardID = new UserCardID();
                userCardID.setUserID(req.getUserAssignedID());
                userCardID.setCardID(req.getCardID());
                userCard.setUserCardID(userCardID);
                userCardRepository.save(userCard);
            }
            else if(req.getUserAssignedID() != null){
                userCardRepository.deleteByCardID(card.getCardID());
                UserCard userCard = new UserCard();
                UserCardID userCardID = new UserCardID();
                userCardID.setUserID(req.getUserAssignedID());
                userCardID.setCardID(req.getCardID());
                userCard.setUserCardID(userCardID);
                userCardRepository.save(userCard);
            }
            else{
                if(card.getCreateByUserID() == req.getUserID()){
                    userCardRepository.deleteByCardID(card.getCardID());
                }
            }
        }
        return card.getCardID();
    }


    public Integer updateCardStatus(RequestUpdateCardStatus req) throws Exception {
        ResponseUserDetails resAssign = userClient.getUserDetails(req.getUserID());
        if(resAssign == null){ //Don't have this userID in database
            throw new Exception("you do not have permission");
        }

        UserCard userCard = userCardRepository.findByUserIDAndCardID(req.getUserID(), req.getCardID());

        if(!IsAdmin(resAssign) && userCard == null){
            throw new Exception("This user is not admin");
        }

        Card card = cardRepository.findByCardID(req.getCardID());
        if(card == null){
            throw new Exception("this cardID is not in database");
        }
        Instant instant = Instant.now();

        if(card.getCreateByUserID() != req.getUserID() && userCard == null){
            throw new Exception("you do not have permission to edit this card");
        }
        if(card.getStatus().equalsIgnoreCase("todo")){
            card.setStatus("doing");
        }
        else if(card.getStatus().equalsIgnoreCase("doing")){
            card.setStatus("done");
        }
        else{
            throw new Exception("error to update");
        }
        card.setUpdateAt(Date.from(instant));
        cardRepository.save(card);
        return card.getCardID();
    }

    public ResponseCardList getCardList(RequestCardList req) throws Exception {
        List<ResponseUserCardList> responseData = cardListRepository.findCardList(req);
        if(responseData == null){
            throw new Exception("Card not found from search");
        }
        ResponseCardList res = new ResponseCardList();
        List<ResponseCardDetails> responseCardDetailsList = new ArrayList<>();
        Integer totalItem = 0;
        for(ResponseUserCardList c : responseData){
            ResponseCardDetails responseCard =  new ResponseCardDetails();
            ResponseUserDetails resAssign = userClient.getUserDetails(c.getCreateByUserID());
            responseCard.setCardID(c.getCardID());
            responseCard.setCardName(c.getCardName());
            responseCard.setCardDetails(c.getCardDetails());
            responseCard.setStatus(c.getStatus());
            responseCard.setCreatedAt(c.getCreateAt());
            if(resAssign != null){
                responseCard.setCreatedUserFullName(resAssign.getFullName());
            }
            if(c.getUserAssignedID() != null){
                ResponseUserDetails resAssigned = userClient.getUserDetails(c.getUserAssignedID());
                if(resAssigned != null){
                    responseCard.setAssignedUserFullName(resAssigned.getFullName());
                }
            }
            responseCardDetailsList.add(responseCard);
            totalItem++;
        }
        res.setResponseCardList(responseCardDetailsList);
        res.setTotalItem(totalItem);

        return res ;

    }

    public ResponseUserCardDetails getCardDetails(Integer cardID) throws Exception {
        Card card = cardRepository.findByCardID(cardID);
        if(card == null){
            throw new Exception("not found this cardID");
        }
        ResponseUserDetails resAssign = userClient.getUserDetails(card.getCreateByUserID());
        if(resAssign == null){
            throw new Exception("Have not this userAssign in database");
        }

        ResponseUserCardDetails responseUserCardDetails = new ResponseUserCardDetails();

        responseUserCardDetails.setCardName(card.getCardName());
        responseUserCardDetails.setCardDetails(card.getCardDetails());
        responseUserCardDetails.setStatus(card.getStatus());
        responseUserCardDetails.setCreateAt(card.getCreateAt());
        responseUserCardDetails.setUpdateAt(card.getUpdateAt());
        responseUserCardDetails.setCreateByUserId(card.getCreateByUserID());
        responseUserCardDetails.setCreateByUserName(resAssign.getFullName());

        UserCard userCard = userCardRepository.findByCardID(cardID);
        if(userCard == null){
            return responseUserCardDetails;
        }
        ResponseUserDetails resAssigned = userClient.getUserDetails(userCard.getUserCardID().getUserID());
        if(resAssigned == null){
            return responseUserCardDetails;
        }
        responseUserCardDetails.setAssignedUserId(resAssigned.getUserID());
        responseUserCardDetails.setAssignedUserName(resAssigned.getFullName());

        return responseUserCardDetails;

    }

    protected boolean IsAdmin(ResponseUserDetails resAssign){
        for(ResponseRole r: resAssign.getResponseRoleList()){
            if(r.getTitle().equalsIgnoreCase("admin")){
                return true;
            }
        }
        return false;
    }


}
