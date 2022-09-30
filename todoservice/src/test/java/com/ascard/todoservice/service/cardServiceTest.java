package com.ascard.todoservice.service;


import com.ascard.todoservice.Entity.Card;
import com.ascard.todoservice.Entity.UserCard;
import com.ascard.todoservice.Entity.embedded.UserCardID;
import com.ascard.todoservice.Repository.CardListRepository;
import com.ascard.todoservice.Repository.CardRepository;
import com.ascard.todoservice.Repository.UserCardRepository;
import com.ascard.todoservice.payload.request.RequestCardList;
import com.ascard.todoservice.payload.request.RequestCreatedCard;
import com.ascard.todoservice.payload.request.RequestEditCardDetails;
import com.ascard.todoservice.payload.request.RequestUpdateCardStatus;
import com.ascard.todoservice.payload.response.ResponseCardList;
import com.ascard.todoservice.payload.response.ResponseRole;
import com.ascard.todoservice.payload.response.ResponseUserCardDetails;
import com.ascard.todoservice.payload.response.ResponseUserCardList;
import com.ascard.todoservice.payload.response.ResponseUserDetails;
import com.ascard.todoservice.service.Client.UserClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class cardServiceTest {

    @InjectMocks
    private CardService cardService;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardListRepository cardListRepository;

    @Mock
    private UserCardRepository userCardRepository;

    @Mock
    private UserClient userClient;

    @Test
    public void whenCreateCard_withCardDetails_ThrowExceptionPermission() throws Exception {

        RequestCreatedCard req = new RequestCreatedCard();
        req.setCardName("todo");
        req.setCardDetails("This is card for todo");
        req.setCreateByUserID(100);
        req.setUserID(23);

        Mockito.when(userClient.getUserDetails(100)).thenThrow(new RuntimeException("you don't have permission"));

        try {
            Integer cardID = cardService.createdCard(req);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals("you don't have permission", e.getMessage());
        }
    }

    @Test
    public void whenCreateCard_withCardDetails_thenThrowNotAdmin() throws Exception {

        ResponseUserDetails user = new ResponseUserDetails();
        user.setUserID(17);
        user.setFullName("Ascend");
        user.setEmail("Ascend@hotmail.com");

        List<ResponseRole> responseRoleList = new ArrayList<>();
        ResponseRole res = new ResponseRole();
        res.setRoleID(2);
        res.setTitle("user");
        responseRoleList.add(res);

        user.setResponseRoleList(responseRoleList);

        RequestCreatedCard req = new RequestCreatedCard();
        req.setCardName("todo");
        req.setCardDetails("This is card for todo");
        req.setCreateByUserID(17);

        Mockito.when(userClient.getUserDetails(17)).thenReturn(user);

        try{
            Integer cardID = cardService.createdCard(req);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals("This user is not admin", e.getMessage());
        }

        Mockito.verify(userClient, Mockito.times(1)).getUserDetails(req.getCreateByUserID());
    }


    @Test
    public void whenCreateCard_withCardDetails_thenSaveCardAndUserCard() throws Exception {

        ResponseUserDetails user = new ResponseUserDetails();
        user.setUserID(17);
        user.setFullName("Ascend");
        user.setEmail("Ascend@hotmail.com");

        ResponseUserDetails user1 = new ResponseUserDetails();
        user1.setUserID(23);
        user1.setFullName("AAA");
        user1.setEmail("AAA@hotmail.com");

        List<ResponseRole> responseRoleList = new ArrayList<>();
        ResponseRole res = new ResponseRole();
        res.setRoleID(1);
        res.setTitle("admin");
        responseRoleList.add(res);

        user.setResponseRoleList(responseRoleList);

        RequestCreatedCard req = new RequestCreatedCard();
        req.setCardName("todo");
        req.setCardDetails("This is card for todo");
        req.setCreateByUserID(17);
        req.setUserID(23);


        Mockito.when(userClient.getUserDetails(17)).thenReturn(user);
        Mockito.when(userClient.getUserDetails(23)).thenReturn(user1);

        Integer cardID = cardService.createdCard(req);

        ArgumentCaptor<Card> cardCapture = ArgumentCaptor.forClass(Card.class);
        ArgumentCaptor<UserCard> userCardCapture = ArgumentCaptor.forClass(UserCard.class);

        Mockito.verify(userClient, Mockito.times(1)).getUserDetails(req.getCreateByUserID());
        Mockito.verify(userClient, Mockito.times(1)).getUserDetails(req.getUserID());
        Mockito.verify(cardRepository, Mockito.times(1)).save(cardCapture.capture());
        Mockito.verify(userCardRepository, Mockito.times(1)).save(userCardCapture.capture());

        Assert.assertEquals("todo", cardCapture.getValue().getCardName());
        Assert.assertEquals("This is card for todo", cardCapture.getValue().getCardDetails());
        Assert.assertEquals("todo", cardCapture.getValue().getStatus());
        Assert.assertEquals(17L, cardCapture.getValue().getCreateByUserID().longValue());
        Assert.assertNotNull(cardCapture.getValue().getCreateAt());

        Assert.assertEquals(23L, userCardCapture.getValue().getUserCardID().getUserID().longValue());

    }

    @Test
    public void whenCreateCard_withCardDetails_thenSaveCardButNotSaveUserCard() throws Exception {

        ResponseUserDetails user = new ResponseUserDetails();
        user.setUserID(17);
        user.setFullName("Ascend");
        user.setEmail("Ascend@hotmail.com");

        List<ResponseRole> responseRoleList = new ArrayList<>();
        ResponseRole res = new ResponseRole();
        res.setRoleID(1);
        res.setTitle("admin");
        responseRoleList.add(res);

        user.setResponseRoleList(responseRoleList);

        RequestCreatedCard req = new RequestCreatedCard();
        req.setCardName("todo");
        req.setCardDetails("This is card for todo");
        req.setCreateByUserID(17);


        Mockito.when(userClient.getUserDetails(17)).thenReturn(user);


        Integer cardID = cardService.createdCard(req);

        ArgumentCaptor<Card> cardCapture = ArgumentCaptor.forClass(Card.class);


        Mockito.verify(userClient, Mockito.times(1)).getUserDetails(req.getCreateByUserID());
        Mockito.verify(userClient, Mockito.times(0)).getUserDetails(req.getUserID());
        Mockito.verify(cardRepository, Mockito.times(1)).save(cardCapture.capture());
        Mockito.verify(userCardRepository, Mockito.times(0)).save(Mockito.any(UserCard.class));

        Assert.assertEquals("todo", cardCapture.getValue().getCardName());
        Assert.assertEquals("This is card for todo", cardCapture.getValue().getCardDetails());
        Assert.assertEquals("todo", cardCapture.getValue().getStatus());
        Assert.assertEquals(17L, cardCapture.getValue().getCreateByUserID().longValue());
        Assert.assertNotNull(cardCapture.getValue().getCreateAt());
    }

    @Test
    public void whenCreateCard_withCardDetails_ThrowExceptionUserAssignedNotInDatabase() throws Exception {

        ResponseUserDetails user = new ResponseUserDetails();
        user.setUserID(21);
        user.setFullName("Ascend");
        user.setEmail("Ascend@hotmail.com");

        List<ResponseRole> responseRoleList = new ArrayList<>();
        ResponseRole res = new ResponseRole();
        res.setRoleID(1);
        res.setTitle("admin");
        responseRoleList.add(res);

        user.setResponseRoleList(responseRoleList);

        RequestCreatedCard req = new RequestCreatedCard();
        req.setCardName("todo");
        req.setCardDetails("This is card for todo");
        req.setCreateByUserID(21);
        req.setUserID(23);

        Mockito.when(userClient.getUserDetails(21)).thenReturn(user);
        Mockito.when(userClient.getUserDetails(23)).thenThrow(new RuntimeException("This UserAssigned not in database"));

        try{
            Integer cardID = cardService.createdCard(req);
        }
        catch (Exception e){
            Assert.assertEquals("This UserAssigned not in database", e.getMessage());
        }
    }


    @Test
    public void whenEditCardDetails_withRequestEditCardDetails_ThrowExceptionPermission() throws Exception {

        RequestEditCardDetails req = new RequestEditCardDetails();
        req.setUserID(17);
        req.setCardID(14);
        req.setCardDetails("Yare Yare");
        req.setCardName("doing");
        req.setUserAssignedID(23);

        Mockito.when(userClient.getUserDetails(17)).thenThrow(new RuntimeException("you do not have permission"));

        try{
            Integer cardID = cardService.editCardDetails(req);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals("you do not have permission", e.getMessage());
        }
    }

    @Test
    public void whenEditCardDetails_withRequestEditCardDetails_ThrowExceptionCardNotInDatabase() throws Exception {
        RequestEditCardDetails req = new RequestEditCardDetails();
        req.setUserID(17);
        req.setCardID(14);
        req.setCardDetails("Yare Yare");
        req.setCardName("doing");
        req.setUserAssignedID(23);

        ResponseUserDetails user = new ResponseUserDetails();
        user.setUserID(17);
        user.setFullName("Ascend");
        user.setEmail("Ascend@hotmail.com");


        Mockito.when(userClient.getUserDetails(17)).thenReturn(user);
        Mockito.when(cardRepository.findByCardID(req.getCardID())).thenReturn(null);


        try{
            Integer cardID = cardService.editCardDetails(req);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals("this card not in database", e.getMessage());
        }

        Mockito.verify(userClient, Mockito.times(1)).getUserDetails(req.getUserID());
        Mockito.verify(cardRepository, Mockito.times(1)).findByCardID(req.getCardID());
    }

    @Test
    public void whenEditCardDetails_withRequestEditCardDetails_thenEditCardButNotEditUserCard() throws Exception {
        RequestEditCardDetails req = new RequestEditCardDetails();
        req.setUserID(17);
        req.setCardID(10);
        req.setCardDetails("New Yare");
        req.setCardName("done");
        req.setUserAssignedID(17);

        ResponseUserDetails user = new ResponseUserDetails();
        user.setUserID(17);
        user.setFullName("Ascend");
        user.setEmail("Ascend@hotmail.com");

        List<ResponseRole> responseRoleList = new ArrayList<>();
        ResponseRole res = new ResponseRole();
        res.setRoleID(2);
        res.setTitle("user");
        responseRoleList.add(res);

        user.setResponseRoleList(responseRoleList);

        Card card = new Card();
        card.setCardID(10);
        card.setCardName("doing");
        card.setCardDetails("Yare Yare");

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);

        Mockito.when(userClient.getUserDetails(17)).thenReturn(user);
        Mockito.when(cardRepository.findByCardID(req.getCardID())).thenReturn(card);

        Integer cardID = cardService.editCardDetails(req);

        Mockito.verify(cardRepository, Mockito.times(1)).findByCardID(req.getCardID());
        Mockito.verify(cardRepository, Mockito.times(1)).save(cardCaptor.capture());

        Assert.assertEquals("done", cardCaptor.getValue().getCardName());
        Assert.assertEquals("New Yare", cardCaptor.getValue().getCardDetails());
    }


    @Test
    public void whenEditCardDetails_withRequestEditCardDetails_thenEditCardAndAddUserCard() throws Exception {
        RequestEditCardDetails req = new RequestEditCardDetails();
        req.setUserID(17);
        req.setCardID(10);
        req.setCardDetails("New Yare");
        req.setCardName("done");
        req.setUserAssignedID(17);

        ResponseUserDetails user = new ResponseUserDetails();
        user.setUserID(17);
        user.setFullName("Ascend");
        user.setEmail("Ascend@hotmail.com");

        List<ResponseRole> responseRoleList = new ArrayList<>();
        ResponseRole res = new ResponseRole();
        res.setRoleID(1);
        res.setTitle("admin");
        responseRoleList.add(res);

        user.setResponseRoleList(responseRoleList);

        Card card = new Card();
        card.setCardID(10);
        card.setCardName("doing");
        card.setCardDetails("Yare Yare");

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        ArgumentCaptor<UserCard> userCardCaptor = ArgumentCaptor.forClass(UserCard.class);

        Mockito.when(userClient.getUserDetails(17)).thenReturn(user);
        Mockito.when(cardRepository.findByCardID(req.getCardID())).thenReturn(card);

        Integer cardID = cardService.editCardDetails(req);

        Mockito.verify(cardRepository, Mockito.times(1)).findByCardID(req.getCardID());
        Mockito.verify(cardRepository, Mockito.times(1)).save(cardCaptor.capture());
        Mockito.verify(userCardRepository, Mockito.times(1)).save(userCardCaptor.capture());

        Assert.assertEquals("done", cardCaptor.getValue().getCardName());
        Assert.assertEquals("New Yare", cardCaptor.getValue().getCardDetails());

    }

    @Test
    public void whenEditCardDetails_withRequestEditCardDetails_thenEditCardAndUserCard() throws Exception {
        RequestEditCardDetails req = new RequestEditCardDetails();
        req.setUserID(17);
        req.setCardID(10);
        req.setCardDetails("New Yare");
        req.setCardName("done");
        req.setUserAssignedID(17);

        ResponseUserDetails user = new ResponseUserDetails();
        user.setUserID(17);
        user.setFullName("Ascend");
        user.setEmail("Ascend@hotmail.com");

        List<ResponseRole> responseRoleList = new ArrayList<>();
        ResponseRole res = new ResponseRole();
        res.setRoleID(1);
        res.setTitle("admin");
        responseRoleList.add(res);

        user.setResponseRoleList(responseRoleList);

        UserCard uCard = new UserCard();

        Card card = new Card();
        card.setCardID(10);
        card.setCardName("doing");
        card.setCardDetails("Yare Yare");
        card.setUserCard(uCard);

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        ArgumentCaptor<UserCard> userCardCaptor = ArgumentCaptor.forClass(UserCard.class);

        Mockito.when(userClient.getUserDetails(17)).thenReturn(user);
        Mockito.when(cardRepository.findByCardID(req.getCardID())).thenReturn(card);

        Integer cardID = cardService.editCardDetails(req);

        Mockito.verify(cardRepository, Mockito.times(1)).findByCardID(req.getCardID());
        Mockito.verify(cardRepository, Mockito.times(1)).save(cardCaptor.capture());
        Mockito.verify(userCardRepository, Mockito.times(1)).deleteByCardID(card.getCardID());
        Mockito.verify(userCardRepository, Mockito.times(1)).save(userCardCaptor.capture());

        Assert.assertEquals("done", cardCaptor.getValue().getCardName());
        Assert.assertEquals("New Yare", cardCaptor.getValue().getCardDetails());

    }

    @Test
    public void whenEditCardDetails_withRequestEditCardDetails_thenEditCardAndDeleteUserCard() throws Exception {
        RequestEditCardDetails req = new RequestEditCardDetails();
        req.setUserID(17);
        req.setCardID(10);
        req.setCardDetails("New Yare");
        req.setCardName("done");

        ResponseUserDetails user = new ResponseUserDetails();
        user.setUserID(17);
        user.setFullName("Ascend");
        user.setEmail("Ascend@hotmail.com");

        List<ResponseRole> responseRoleList = new ArrayList<>();
        ResponseRole res = new ResponseRole();
        res.setRoleID(1);
        res.setTitle("admin");
        responseRoleList.add(res);

        user.setResponseRoleList(responseRoleList);

        UserCard uCard = new UserCard();

        Card card = new Card();
        card.setCardID(10);
        card.setCardName("doing");
        card.setCardDetails("Yare Yare");
        card.setCreateByUserID(17);
        card.setUserCard(uCard);

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        ArgumentCaptor<UserCard> userCardCaptor = ArgumentCaptor.forClass(UserCard.class);

        Mockito.when(userClient.getUserDetails(17)).thenReturn(user);
        Mockito.when(cardRepository.findByCardID(req.getCardID())).thenReturn(card);

        Integer cardID = cardService.editCardDetails(req);

        Mockito.verify(cardRepository, Mockito.times(1)).findByCardID(req.getCardID());
        Mockito.verify(cardRepository, Mockito.times(1)).save(cardCaptor.capture());
        Mockito.verify(userCardRepository, Mockito.times(1)).deleteByCardID(card.getCardID());
        Mockito.verify(userCardRepository, Mockito.times(0)).save(userCardCaptor.capture());

        Assert.assertEquals("done", cardCaptor.getValue().getCardName());
        Assert.assertEquals("New Yare", cardCaptor.getValue().getCardDetails());

    }



    @Test
    public void whenUpdateCardStatus_withRequestUpdateCardStatus_ReturnCardID() throws Exception {
        ResponseUserDetails user = new ResponseUserDetails();
        user.setUserID(17);
        user.setFullName("Ascend");
        user.setEmail("Ascend@hotmail.com");

        List<ResponseRole> responseRoleList = new ArrayList<>();
        ResponseRole res = new ResponseRole();
        res.setRoleID(1);
        res.setTitle("admin");
        responseRoleList.add(res);

        user.setResponseRoleList(responseRoleList);

        Card card = new Card();
        card.setCardID(12);
        card.setStatus("todo");
        card.setCreateByUserID(17);

        RequestUpdateCardStatus req = new RequestUpdateCardStatus();
        req.setCardID(12);
        req.setUserID(17);

        Mockito.when(userClient.getUserDetails(17)).thenReturn(user);
        Mockito.when(cardRepository.findByCardID(12)).thenReturn(card);
        Mockito.when(cardRepository.save(card)).thenReturn(null);

        Integer cardID = cardService.updateCardStatus(req);

        Assert.assertEquals(Optional.of(12),Optional.of(cardID));
        Assert.assertEquals("doing", card.getStatus());

    }

    @Test
    public void whenUpdateCardStatus_withRequestUpdateCardStatus_ThrowExceptionPermission() throws Exception{
        ResponseUserDetails user = new ResponseUserDetails();
        user.setUserID(17);
        user.setFullName("Ascend");
        user.setEmail("Ascend@hotmail.com");

        RequestUpdateCardStatus req = new RequestUpdateCardStatus();
        req.setUserID(17);
        req.setCardID(11);

        try{
            Integer cardID = cardService.updateCardStatus(req);
        }
        catch (Exception e){
            Assert.assertEquals("you do not have permission", e.getMessage());
        }

        Mockito.verify(userClient, Mockito.times(1)).getUserDetails(req.getUserID());
    }

    @Test
    public void whenUpdateCardStatus_withRequestUpdateCardStatus_ThrowExceptionNotAdmin() throws Exception{
        ResponseUserDetails user = new ResponseUserDetails();
        user.setUserID(17);
        user.setFullName("Ascend");
        user.setEmail("Ascend@hotmail.com");

        List<ResponseRole> responseRoleList = new ArrayList<>();
        ResponseRole res = new ResponseRole();
        res.setRoleID(2);
        res.setTitle("user");
        responseRoleList.add(res);

        user.setResponseRoleList(responseRoleList);

        RequestUpdateCardStatus req = new RequestUpdateCardStatus();
        req.setUserID(17);
        req.setCardID(11);


        Mockito.when(userClient.getUserDetails(17)).thenReturn(user);
        Mockito.when(userCardRepository.findByUserIDAndCardID(req.getUserID(), req.getCardID())).thenReturn(null);

        try{
            Integer cardID = cardService.updateCardStatus(req);
        }
        catch (Exception e){
            Assert.assertEquals("This user is not admin", e.getMessage());
        }
        Mockito.verify(userClient, Mockito.times(1)).getUserDetails(req.getUserID());
    }

    @Test
    public void whenUpdateCardStatus_withRequestUpdateCardStatus_ThrowExceptionCardNotInDatabase() throws Exception{
        ResponseUserDetails user = new ResponseUserDetails();
        user.setUserID(17);
        user.setFullName("Ascend");
        user.setEmail("Ascend@hotmail.com");

        List<ResponseRole> responseRoleList = new ArrayList<>();
        ResponseRole res = new ResponseRole();
        res.setRoleID(1);
        res.setTitle("admin");
        responseRoleList.add(res);

        user.setResponseRoleList(responseRoleList);

        RequestUpdateCardStatus req = new RequestUpdateCardStatus();
        req.setUserID(17);
        req.setCardID(11);

        UserCard userCard = new UserCard();

        Mockito.when(userClient.getUserDetails(17)).thenReturn(user);
        Mockito.when(userCardRepository.findByUserIDAndCardID(req.getUserID(), req.getCardID())).thenReturn(userCard);
        Mockito.when(cardRepository.findByCardID(req.getCardID())).thenReturn(null);

        try{
            Integer cardID = cardService.updateCardStatus(req);
        }
        catch (Exception e){
            Assert.assertEquals("this cardID is not in database", e.getMessage());
        }

        Mockito.verify(userClient, Mockito.times(1)).getUserDetails(req.getUserID());
        Mockito.verify(userCardRepository, Mockito.times(1)).findByUserIDAndCardID(req.getUserID(), req.getCardID());

    }

    @Test
    public void whenUpdateCardStatus_withRequestUpdateCardStatus_ThrowExceptionNoPermissionToUpdateCardStatus() throws Exception{
        ResponseUserDetails user = new ResponseUserDetails();
        user.setUserID(17);
        user.setFullName("Ascend");
        user.setEmail("Ascend@hotmail.com");

        List<ResponseRole> responseRoleList = new ArrayList<>();
        ResponseRole res = new ResponseRole();
        res.setRoleID(1);
        res.setTitle("admin");
        responseRoleList.add(res);

        user.setResponseRoleList(responseRoleList);

        RequestUpdateCardStatus req = new RequestUpdateCardStatus();
        req.setUserID(17);
        req.setCardID(11);

        Card card = new Card();
        card.setCardID(11);
        card.setCreateByUserID(23);


        Mockito.when(userClient.getUserDetails(17)).thenReturn(user);
        Mockito.when(userCardRepository.findByUserIDAndCardID(req.getUserID(), req.getCardID())).thenReturn(null);
        Mockito.when(cardRepository.findByCardID(req.getCardID())).thenReturn(card);

        try{
            Integer cardID = cardService.updateCardStatus(req);
        }
        catch (Exception e){
            Assert.assertEquals("you do not have permission to edit this card", e.getMessage());
        }

        Mockito.verify(userClient, Mockito.times(1)).getUserDetails(req.getUserID());
        Mockito.verify(userCardRepository, Mockito.times(1)).findByUserIDAndCardID(req.getUserID(), req.getCardID());

    }

    @Test
    public void whenUpdateCardStatus_withRequestUpdateCardStatus_thenUpdateCardStatusDoing() throws Exception{
        ResponseUserDetails user = new ResponseUserDetails();
        user.setUserID(17);
        user.setFullName("Ascend");
        user.setEmail("Ascend@hotmail.com");

        List<ResponseRole> responseRoleList = new ArrayList<>();
        ResponseRole res = new ResponseRole();
        res.setRoleID(1);
        res.setTitle("admin");
        responseRoleList.add(res);

        user.setResponseRoleList(responseRoleList);

        RequestUpdateCardStatus req = new RequestUpdateCardStatus();
        req.setUserID(17);
        req.setCardID(11);

        Card card = new Card();
        card.setCardID(11);
        card.setStatus("todo");
        card.setCreateByUserID(23);

        UserCard userCard = new UserCard();

        Mockito.when(userClient.getUserDetails(17)).thenReturn(user);
        Mockito.when(userCardRepository.findByUserIDAndCardID(req.getUserID(), req.getCardID())).thenReturn(userCard);
        Mockito.when(cardRepository.findByCardID(req.getCardID())).thenReturn(card);

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);

        try{
            Integer cardID = cardService.updateCardStatus(req);
        }
        catch (Exception e){
            Assert.assertEquals("you do not have permission to edit this card", e.getMessage());
        }

        Mockito.verify(userClient, Mockito.times(1)).getUserDetails(req.getUserID());
        Mockito.verify(userCardRepository, Mockito.times(1)).findByUserIDAndCardID(req.getUserID(), req.getCardID());
        Mockito.verify(cardRepository, Mockito.times(1)).save(cardCaptor.capture());
    }

    @Test
    public void whenUpdateCardStatus_withRequestUpdateCardStatus_thenUpdateCardStatusDone() throws Exception{
        ResponseUserDetails user = new ResponseUserDetails();
        user.setUserID(17);
        user.setFullName("Ascend");
        user.setEmail("Ascend@hotmail.com");

        List<ResponseRole> responseRoleList = new ArrayList<>();
        ResponseRole res = new ResponseRole();
        res.setRoleID(1);
        res.setTitle("admin");
        responseRoleList.add(res);

        user.setResponseRoleList(responseRoleList);

        RequestUpdateCardStatus req = new RequestUpdateCardStatus();
        req.setUserID(17);
        req.setCardID(11);

        Card card = new Card();
        card.setCardID(11);
        card.setStatus("doing");
        card.setCreateByUserID(23);

        UserCard userCard = new UserCard();

        Mockito.when(userClient.getUserDetails(17)).thenReturn(user);
        Mockito.when(userCardRepository.findByUserIDAndCardID(req.getUserID(), req.getCardID())).thenReturn(userCard);
        Mockito.when(cardRepository.findByCardID(req.getCardID())).thenReturn(card);

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);

        try{
            Integer cardID = cardService.updateCardStatus(req);
        }
        catch (Exception e){
            Assert.assertEquals("error to update", e.getMessage());
        }

        Mockito.verify(userClient, Mockito.times(1)).getUserDetails(req.getUserID());
        Mockito.verify(userCardRepository, Mockito.times(1)).findByUserIDAndCardID(req.getUserID(), req.getCardID());
        Mockito.verify(cardRepository, Mockito.times(1)).save(cardCaptor.capture());
    }

    @Test
    public void whenUpdateCardStatus_withRequestUpdateCardStatus_ThrowExceptionErrorToUpdate() throws Exception{
        ResponseUserDetails user = new ResponseUserDetails();
        user.setUserID(17);
        user.setFullName("Ascend");
        user.setEmail("Ascend@hotmail.com");

        List<ResponseRole> responseRoleList = new ArrayList<>();
        ResponseRole res = new ResponseRole();
        res.setRoleID(1);
        res.setTitle("admin");
        responseRoleList.add(res);

        user.setResponseRoleList(responseRoleList);

        RequestUpdateCardStatus req = new RequestUpdateCardStatus();
        req.setUserID(17);
        req.setCardID(11);

        Card card = new Card();
        card.setCardID(11);
        card.setStatus("done");
        card.setCreateByUserID(23);

        UserCard userCard = new UserCard();

        Mockito.when(userClient.getUserDetails(17)).thenReturn(user);
        Mockito.when(userCardRepository.findByUserIDAndCardID(req.getUserID(), req.getCardID())).thenReturn(userCard);
        Mockito.when(cardRepository.findByCardID(req.getCardID())).thenReturn(card);

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);

        try{
            Integer cardID = cardService.updateCardStatus(req);
        }
        catch (Exception e){
            Assert.assertEquals("error to update", e.getMessage());
        }

        Mockito.verify(userClient, Mockito.times(1)).getUserDetails(req.getUserID());
        Mockito.verify(userCardRepository, Mockito.times(1)).findByUserIDAndCardID(req.getUserID(), req.getCardID());
        Mockito.verify(cardRepository, Mockito.times(0)).save(cardCaptor.capture());
    }

    @Test
    public void whenGetCardDetails_withCardID_thenThrowExceptionCardNotFound() throws Exception {

        Mockito.when(cardRepository.findByCardID(7)).thenReturn(null);

        try{
            ResponseUserCardDetails cardDetails = cardService.getCardDetails(7);
        }
        catch (Exception e){
            Assert.assertEquals("not found this cardID", e.getMessage());
        }
    }

    @Test
    public void whenGetCardDetails_withCardID_thenReturnOnlyCardDetails() throws Exception {

        ResponseUserDetails userAssign = new ResponseUserDetails();
        userAssign.setUserID(17);
        userAssign.setFullName("Ascend");
        userAssign.setEmail("Ascend@hotmail.com");

        Card card = new Card();
        card.setCardID(7);
        card.setCardName("todo");
        card.setCardDetails("This is card for todo");
        card.setCreateByUserID(17);

        Mockito.when(cardRepository.findByCardID(7)).thenReturn(card);
        Mockito.when(userClient.getUserDetails(card.getCreateByUserID())).thenReturn(userAssign);
        Mockito.when(userCardRepository.findByCardID(card.getCardID())).thenReturn(null);

        try{
            ResponseUserCardDetails res = cardService.getCardDetails(7);
            Assert.assertEquals("todo", res.getCardName());
            Assert.assertEquals("This is card for todo", res.getCardDetails());
        }
        catch (Exception e){
            Assert.assertEquals("Error to get details", e.getMessage());
        }
    }

    @Test
    public void whenGetCardDetails_withCardID_thenThrowExceptionUserAssignedNotFound() throws Exception {

        ResponseUserDetails userAssign = new ResponseUserDetails();
        userAssign.setUserID(17);
        userAssign.setFullName("Ascend");
        userAssign.setEmail("Ascend@hotmail.com");

        Card card = new Card();
        card.setCardID(7);
        card.setCardName("todo");
        card.setCardDetails("This is card for todo");
        card.setCreateByUserID(17);

        UserCardID userCardID = new UserCardID();

        UserCard userCard = new UserCard();
        userCard.setUserCardID(userCardID);

        Mockito.when(cardRepository.findByCardID(7)).thenReturn(card);
        Mockito.when(userClient.getUserDetails(card.getCreateByUserID())).thenReturn(userAssign);
        Mockito.when(userCardRepository.findByCardID(card.getCardID())).thenReturn(userCard);

        try{
            ResponseUserCardDetails res = cardService.getCardDetails(7);
            Assert.assertEquals("todo", res.getCardName());
            Assert.assertEquals("This is card for todo", res.getCardDetails());
        }
        catch (Exception e){
            Assert.assertEquals("Error to get details", e.getMessage());
        }
    }

    @Test
    public void whenGetCardDetails_withCardID_thenReturnCardAndUserCardDetails() throws Exception {

        ResponseUserDetails userAssign = new ResponseUserDetails();
        userAssign.setUserID(17);
        userAssign.setFullName("Ascend");
        userAssign.setEmail("Ascend@hotmail.com");

        ResponseUserDetails userAssigned = new ResponseUserDetails();
        userAssigned.setUserID(21);
        userAssigned.setFullName("ADA");
        userAssigned.setEmail("ADA@hotmail.com");

        UserCard userCard = new UserCard();
        UserCardID userCardID = new UserCardID();
        userCardID.setUserID(21);
        userCard.setUserCardID(userCardID);

        Card card = new Card();
        card.setCardID(7);
        card.setCardName("todo");
        card.setCardDetails("This is card for todo");
        card.setCreateByUserID(17);
        card.setUserCard(userCard);


        Mockito.when(cardRepository.findByCardID(7)).thenReturn(card);
        Mockito.when(userClient.getUserDetails(card.getCreateByUserID())).thenReturn(userAssign);
        Mockito.when(userCardRepository.findByCardID(card.getCardID())).thenReturn(userCard);
        Mockito.when(userClient.getUserDetails(card.getUserCard().getUserCardID().getUserID())).thenReturn(userAssigned);

        try{
            ResponseUserCardDetails res = cardService.getCardDetails(7);
            Assert.assertEquals("todo", res.getCardName());
            Assert.assertEquals("This is card for todo", res.getCardDetails());
        }
        catch (Exception e){
            Assert.assertEquals("Error to get details", e.getMessage());
        }
    }


    @Test
    public void whenGetCardList_withRequestCardList_thenThrowCardNotFoundFromSearch() throws Exception {
        RequestCardList req = new RequestCardList();
        req.setSearch("D");
        req.setCreatedByUserID(17);

        Mockito.when(cardListRepository.findCardList(req)).thenReturn(null);

        try{
            ResponseCardList card = cardService.getCardList(req);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals("Card not found from search", e.getMessage());
        }
    }


    @Test
    public void whenGetCardList_withRequestCardList_Return() throws Exception {
        RequestCardList req = new RequestCardList();
        req.setSearch("D");
        req.setCreatedByUserID(17);
        req.setAssignedUserID(21);

        ResponseUserDetails userAssign = new ResponseUserDetails();
        userAssign.setUserID(17);
        userAssign.setFullName("Ascend");
        userAssign.setEmail("Ascend@hotmail.com");

//        ResponseUserDetails userAssign1 = new ResponseUserDetails();
//        userAssign1.setUserID(18);
//        userAssign1.setFullName("Monkey");
//        userAssign1.setEmail("Monkey@hotmail.com");

        ResponseUserDetails userAssigned = new ResponseUserDetails();
        userAssigned.setUserID(21);
        userAssigned.setFullName("ADA");
        userAssigned.setEmail("ADA@hotmail.com");

//        ResponseUserDetails userAssigned1 = new ResponseUserDetails();
//        userAssigned1.setUserID(22);
//        userAssigned1.setFullName("Mike");
//        userAssigned1.setEmail("Mike@hotmail.com");

        List<ResponseUserCardList> responseCardLists = new ArrayList<>();

        ResponseUserCardList res = new ResponseUserCardList();
        res.setCardID(10);
        res.setCardName("todo");
        res.setStatus("todo");
        res.setCreateByUserID(17);
        res.setUserAssignedID(21);

        responseCardLists.add(res);

        Mockito.when(cardListRepository.findCardList(req)).thenReturn(responseCardLists);
        Mockito.when(userClient.getUserDetails(req.getCreatedByUserID())).thenReturn(userAssign);
        Mockito.when(userClient.getUserDetails(req.getAssignedUserID())).thenReturn(userAssigned);


        try{
            ResponseCardList card = cardService.getCardList(req);
            Assert.assertEquals(10L, card.getResponseCardList().get(0).getCardID().longValue());
            Assert.assertEquals("todo", card.getResponseCardList().get(0).getCardName());
            Assert.assertEquals("Ascend", card.getResponseCardList().get(0).getCreatedUserFullName());
            Assert.assertEquals("todo", card.getResponseCardList().get(0).getStatus());
            Assert.assertEquals("ADA", card.getResponseCardList().get(0).getAssignedUserFullName());

        }
        catch (Exception e){
            Assert.assertEquals("Card not found from search", e.getMessage());
        }

    }

}
