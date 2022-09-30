package com.ascard.todoservice.controller;


import com.ascard.todoservice.payload.request.RequestCardList;
import com.ascard.todoservice.payload.request.RequestCreatedCard;
import com.ascard.todoservice.payload.request.RequestEditCardDetails;
import com.ascard.todoservice.payload.request.RequestUpdateCardStatus;
import com.ascard.todoservice.payload.response.ResponseCardDetails;
import com.ascard.todoservice.payload.response.ResponseCardList;
import com.ascard.todoservice.payload.response.ResponseUserCardDetails;
import com.ascard.todoservice.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class cardControllerTest {

    @InjectMocks
    private CardController cardController;

    @Mock
    private CardService cardService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup(){
        mockMvc = MockMvcBuilders.standaloneSetup(cardController).setControllerAdvice(new ControllerAdvicer()).build();
    }

    @Test
    public void getCardList_withRequestCardList_shouldReturnStatusOkAndResponseCardList() throws Exception {
        String url = "/api/card/list/?search=d&page=1&pageSize=10&sortBy=cardID";

        RequestCardList req = new RequestCardList();
        req.setSearch("d");
        req.setSortBy("cardID");
        req.setPage(1);
        req.setPageSize(10);


        ResponseCardDetails responseCardDetails = new ResponseCardDetails();
        responseCardDetails.setCardID(10);
        responseCardDetails.setCardName("Todo");
        responseCardDetails.setCardDetails("This is card for todo");
        responseCardDetails.setStatus("todo");
        responseCardDetails.setCreatedUserFullName("Madara");
        responseCardDetails.setAssignedUserFullName("Tobirama");

        List<ResponseCardDetails> responseCardDetailsList = new ArrayList<>();
        responseCardDetailsList.add(responseCardDetails);

        ResponseCardList res = new ResponseCardList();
        res.setResponseCardList(responseCardDetailsList);

        Mockito.when(cardService.getCardList(req)).thenReturn(res);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(url);

//        MvcResult mvcResult = mockMvc.perform(builder).andReturn();
//
//        String responseBase = mvcResult.getResponse().getContentAsString() ;
//
//        System.out.println(responseBase);


        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.responseCardList[0].cardID", Matchers.is(10)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.responseCardList[0].cardName", Matchers.is("Todo")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.responseCardList[0].cardDetails", Matchers.is("This is card for todo")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.responseCardList[0].createdUserFullName", Matchers.is("Madara")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.responseCardList[0].assignedUserFullName", Matchers.is("Tobirama")));

        Mockito.verify(cardService, Mockito.times(1)).getCardList(req);
    }

    @Test
    public void getCardList_RequestCardListWithNullSortBy_shouldReturnErrorWithBadRequest() throws Exception {
        String url = "/api/card/list/";

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get(url)
                .param("search", "d")
                .param("page", "1")
                .param("pageSize", "10");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("sortBy is required")));
    }


    @Test
    public void getCardList_RequestCardListWithNullPage_shouldReturnErrorWithBadRequest() throws Exception {
        String url = "/api/card/list/?search=d&pageSize=10&sortBy=cardID";

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(url);

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("page is required")));
    }


    @Test
    public void getCardList_RequestCardListWithNullPageSize_shouldReturnErrorWithBadRequest() throws Exception {
        String url = "/api/card/list/?search=d&sortBy=cardID&page=1";

        RequestCardList req = new RequestCardList();
        req.setSearch("d");
        req.setSortBy("cardID");
        req.setPage(1);

        ResponseCardDetails responseCardDetails = new ResponseCardDetails();
        responseCardDetails.setCardID(10);
        responseCardDetails.setCardName("Todo");
        responseCardDetails.setCardDetails("This is card for todo");
        responseCardDetails.setStatus("todo");
        responseCardDetails.setCreatedUserFullName("Madara");
        responseCardDetails.setAssignedUserFullName("Tobirama");

        List<ResponseCardDetails> responseCardDetailsList = new ArrayList<>();
        responseCardDetailsList.add(responseCardDetails);

        ResponseCardList res = new ResponseCardList();
        res.setResponseCardList(responseCardDetailsList);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get(url)
                .content(objectMapper.writeValueAsString(req));

        MvcResult mvcResult = mockMvc.perform(builder).andReturn();

        String responseBase = mvcResult.getResponse().getContentAsString() ;

        System.out.println(responseBase);

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("pageSize is required")));

    }



    @Test
    public void getCardList_withRequestCardList_shouldThrowExceptionCardNotFound() throws Exception {
        String url = "/api/card/list";

        RequestCardList req = new RequestCardList();
        req.setSortBy("cardID");
        req.setPageSize(10);
        req.setPage(1);

        Mockito.when(cardService.getCardList(req)).thenThrow(new RuntimeException("Card not found from search"));

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get(url)
                .content(objectMapper.writeValueAsString(req))
                .param("sortBy", req.getSortBy())
                .param("pageSize", req.getPageSize().toString())
                .param("page", req.getPage().toString());

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("Card not found from search")));

        Mockito.verify(cardService, Mockito.times(1)).getCardList(req);
    }


    @Test
    public void getCardDetails_withCardId_shouldReturnUserCardDetails() throws Exception {
        String url = "/api/card/{id}/details";

        Integer cardId = 10 ;

        ResponseUserCardDetails res = new ResponseUserCardDetails();
        res.setCardName("Doing");
        res.setCardDetails("This is card for doing");
        res.setStatus("doing");
        res.setCreateByUserName("Naruto");
        res.setAssignedUserName("Sasike");

        Mockito.when(cardService.getCardDetails(cardId)).thenReturn(res);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get(url, cardId)
                .content(objectMapper.writeValueAsString(cardId));

//        MvcResult mvcResult = mockMvc.perform(builder).andReturn();
//
//        String responseBase = mvcResult.getResponse().getContentAsString() ;
//
//        System.out.println(responseBase);

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.cardName", Matchers.is("Doing")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.cardDetails", Matchers.is("This is card for doing")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.status", Matchers.is("doing")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.createByUserName", Matchers.is("Naruto")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.assignedUserName", Matchers.is("Sasike")));

        Mockito.verify(cardService, Mockito.times(1)).getCardDetails(cardId);

    }

    @Test
    public void getCardDetails_withCardId_shouldThrowExceptionCardNotFound() throws Exception {
        String url = "/api/card/{id}/details";

        Integer cardId = 10 ;

        Mockito.when(cardService.getCardDetails(cardId)).thenThrow(new RuntimeException("not found this cardID"));

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get(url, cardId)
                .content(objectMapper.writeValueAsString(cardId));

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("not found this cardID")));

        Mockito.verify(cardService, Mockito.times(1)).getCardDetails(cardId);
    }

    @Test
    public void getCardDetails_withCardId_shouldThrowExceptionUserAssignNotInDatabase() throws Exception {
        String url = "/api/card/{id}/details";

        Integer cardId = 10 ;

        Mockito.when(cardService.getCardDetails(cardId)).thenThrow(new RuntimeException("Have not this userAssign in database"));

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get(url, cardId)
                .content(objectMapper.writeValueAsString(cardId));

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("Have not this userAssign in database")));

        Mockito.verify(cardService, Mockito.times(1)).getCardDetails(cardId);
    }

    @Test
    public void getCardDetails_withCardId_shouldThrowExceptionUserAssignedNotInDatabase() throws Exception {
        String url = "/api/card/{id}/details";

        Integer cardId = 10 ;

        Mockito.when(cardService.getCardDetails(cardId)).thenThrow(new RuntimeException("Have not this userAssigned in database"));

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get(url, cardId)
                .content(objectMapper.writeValueAsString(cardId));

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("Have not this userAssigned in database")));

        Mockito.verify(cardService, Mockito.times(1)).getCardDetails(cardId);
    }

    @Test
    public void createCard_withRequestCreateCard_ReturnCardId() throws Exception {
        String url = "/api/card/create";

        Integer cardId = 10;

        RequestCreatedCard req = new RequestCreatedCard();
        req.setUserID(23);
        req.setCreateByUserID(17);
        req.setCardName("Todo");
        req.setCardDetails("This is card for todo");

        Mockito.when(cardService.createdCard(req)).thenReturn(cardId);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

//        MvcResult mvcResult = mockMvc.perform(builder).andReturn();
//
//        String responseBase = mvcResult.getResponse().getContentAsString() ;
//
//        System.out.println(responseBase);

        mockMvc.perform(builder)
              .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.cardID", Matchers.is(10)));

        Mockito.verify(cardService, Mockito.times(1)).createdCard(req);

    }

    @Test
    public void createCard_RequestCreateCardWithNullCardName_ReturnBadRequest() throws Exception {
        String url = "/api/card/create";

        RequestCreatedCard req = new RequestCreatedCard();
        req.setUserID(23);
        req.setCreateByUserID(17);
        req.setCardDetails("This is card for todo");

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("cardName is required")));

        Mockito.verify(cardService, Mockito.times(0)).createdCard(req);

    }

    @Test
    public void createCard_RequestCreateCardWithNullCardDetails_ReturnBadRequest() throws Exception {
        String url = "/api/card/create";

        RequestCreatedCard req = new RequestCreatedCard();
        req.setUserID(23);
        req.setCreateByUserID(17);
        req.setCardName("Todo");

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("cardDetails is required")));

        Mockito.verify(cardService, Mockito.times(0)).createdCard(req);

    }

    @Test
    public void createCard_RequestCreateCardWithNullCreateByUserId_ReturnBadRequest() throws Exception {
        String url = "/api/card/create";

        RequestCreatedCard req = new RequestCreatedCard();
        req.setUserID(23);
        req.setCardDetails("This is card for todo");
        req.setCardName("Todo");

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("createByUserID is required")));

        Mockito.verify(cardService, Mockito.times(0)).createdCard(req);

    }


    @Test
    public void createCard_withRequestCreateCard_ThrowExceptionDoNotHavePermission() throws Exception {
        String url = "/api/card/create";

        RequestCreatedCard req = new RequestCreatedCard();
        req.setUserID(23);
        req.setCreateByUserID(17);
        req.setCardName("Todo");
        req.setCardDetails("This is card for todo");

        Mockito.when(cardService.createdCard(req)).thenThrow(new RuntimeException("You do not have permission"));

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("You do not have permission")));

        Mockito.verify(cardService, Mockito.times(1)).createdCard(req);

    }

    @Test
    public void createCard_withRequestCreateCard_ThrowExceptionUserIsNotAdmin() throws Exception {
        String url = "/api/card/create";

        RequestCreatedCard req = new RequestCreatedCard();
        req.setUserID(23);
        req.setCreateByUserID(17);
        req.setCardName("Todo");
        req.setCardDetails("This is card for todo");

        Mockito.when(cardService.createdCard(req)).thenThrow(new RuntimeException("This user is not admin"));

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("This user is not admin")));

        Mockito.verify(cardService, Mockito.times(1)).createdCard(req);

    }

    @Test
    public void createCard_withRequestCreateCard_ThrowExceptionUserAssignedNotFoundInDatabase() throws Exception {
        String url = "/api/card/create";

        RequestCreatedCard req = new RequestCreatedCard();
        req.setUserID(23);
        req.setCreateByUserID(17);
        req.setCardName("Todo");
        req.setCardDetails("This is card for todo");

        Mockito.when(cardService.createdCard(req)).thenThrow(new RuntimeException("This userAssigned is not in database"));

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("This userAssigned is not in database")));

        Mockito.verify(cardService, Mockito.times(1)).createdCard(req);

    }

    @Test
    public void editCardDetails_withRequestEditCard_shouldReturnCardId() throws Exception {
        String url = "/api/card/edit";

        Integer cardId = 10;

        RequestEditCardDetails req = new RequestEditCardDetails();
        req.setCardID(10);
        req.setUserAssignedID(21);
        req.setCardDetails("This is card for what");
        req.setCardName("Todoroki");
        req.setUserID(17);

        Mockito.when(cardService.editCardDetails(req)).thenReturn(cardId);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .put(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

//        MvcResult mvcResult = mockMvc.perform(builder).andReturn();
//
//        String responseBase = mvcResult.getResponse().getContentAsString() ;
//
//        System.out.println(responseBase);

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.cardID", Matchers.is(10)));

        Mockito.verify(cardService, Mockito.times(1)).editCardDetails(req);
    }

    @Test
    public void editCardDetails_RequestEditCardWithNullCardId_shouldReturnBadRequest() throws Exception {
        String url = "/api/card/edit";

        RequestEditCardDetails req = new RequestEditCardDetails();
        req.setCardDetails("This is card for todo");
        req.setUserAssignedID(21);
        req.setCardName("Todo");
        req.setUserID(17);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .put(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("cardID is required")));

        Mockito.verify(cardService, Mockito.times(0)).editCardDetails(req);
    }

    @Test
    public void editCardDetails_RequestEditCardWithNullUserId_shouldReturnBadRequest() throws Exception {
        String url = "/api/card/edit";

        RequestEditCardDetails req = new RequestEditCardDetails();
        req.setCardID(10);
        req.setCardDetails("This is card for todo");
        req.setUserAssignedID(21);
        req.setCardName("Todo");

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .put(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("userID is required")));

        Mockito.verify(cardService, Mockito.times(0)).editCardDetails(req);
    }

    @Test
    public void editCardDetails_RequestEditCardWithNullCardName_shouldReturnBadRequest() throws Exception {
        String url = "/api/card/edit";

        RequestEditCardDetails req = new RequestEditCardDetails();
        req.setCardID(10);
        req.setUserAssignedID(21);
        req.setCardDetails("This is card for what");
        req.setUserID(17);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .put(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("cardName is required")));

        Mockito.verify(cardService, Mockito.times(0)).editCardDetails(req);
    }

    @Test
    public void editCardDetails_RequestEditCardWithNullCardDetails_shouldReturnBadRequest() throws Exception {
        String url = "/api/card/edit";

        RequestEditCardDetails req = new RequestEditCardDetails();
        req.setCardID(10);
        req.setUserAssignedID(21);
        req.setCardName("Todo");
        req.setUserID(17);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .put(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("cardDetails is required")));

        Mockito.verify(cardService, Mockito.times(0)).editCardDetails(req);
    }

    @Test
    public void editCardDetails_withRequestEditCard_shouldThrowExceptionDoNotHavePermission() throws Exception {
        String url = "/api/card/edit";

        RequestEditCardDetails req = new RequestEditCardDetails();
        req.setCardID(10);
        req.setUserAssignedID(21);
        req.setCardDetails("This is card for what");
        req.setCardName("Todoroki");
        req.setUserID(17);

        Mockito.when(cardService.editCardDetails(req)).thenThrow(new RuntimeException("you do not have permission"));

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .put(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("you do not have permission")));

        Mockito.verify(cardService, Mockito.times(1)).editCardDetails(req);
    }


    @Test
    public void editCardDetails_withRequestEditCard_shouldThrowExceptionCardNotInDatabase() throws Exception {
        String url = "/api/card/edit";

        RequestEditCardDetails req = new RequestEditCardDetails();
        req.setCardID(10);
        req.setUserAssignedID(21);
        req.setCardDetails("This is card for what");
        req.setCardName("Todoroki");
        req.setUserID(17);

        Mockito.when(cardService.editCardDetails(req)).thenThrow(new RuntimeException("this card not in database"));

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .put(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("this card not in database")));

        Mockito.verify(cardService, Mockito.times(1)).editCardDetails(req);
    }

    @Test
    public void updateCardStatus_withRequestUpdateCardStatus_shouldReturnCardId() throws Exception {
        String url = "/api/card/update";

        Integer cardId = 10;

        RequestUpdateCardStatus req = new RequestUpdateCardStatus();
        req.setCardID(10);
        req.setUserID(17);

        Mockito.when(cardService.updateCardStatus(req)).thenReturn(cardId);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .put(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.cardID", Matchers.is(10)));

        Mockito.verify(cardService, Mockito.times(1)).updateCardStatus(req);
    }

    @Test
    public void updateCardStatus_RequestUpdateCardStatusWithNullUserID_shouldReturnBadRequest() throws Exception {
        String url = "/api/card/update";

        RequestUpdateCardStatus req = new RequestUpdateCardStatus();
        req.setCardID(10);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .put(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("userID is required")));

    }

    @Test
    public void updateCardStatus_RequestUpdateCardStatusWithNullCardID_shouldBadRequest() throws Exception {
        String url = "/api/card/update";

        RequestUpdateCardStatus req = new RequestUpdateCardStatus();
        req.setUserID(10);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .put(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("cardID is required")));
    }

    @Test
    @Ignore
    public void updateCardStatus_RequestUpdateCardStatusWithNullUserIDAndCardID_shouldBadRequest() throws Exception {
        String url = "/api/card/update";

        RequestUpdateCardStatus req = new RequestUpdateCardStatus();

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .put(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("cardID is required")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[1]", Matchers.is("userID is required")));
    }


    @Test
    public void updateCardStatus_withRequestUpdateCardStatus_shouldThrowExceptionUserIsNotAdmin() throws Exception {
        String url = "/api/card/update";

        RequestUpdateCardStatus req = new RequestUpdateCardStatus();
        req.setCardID(10);
        req.setUserID(17);

        Mockito.when(cardService.updateCardStatus(req)).thenThrow(new RuntimeException("This user is not admin"));

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .put(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("This user is not admin")));

        Mockito.verify(cardService, Mockito.times(1)).updateCardStatus(req);
    }



}
