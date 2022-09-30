package com.asgard.user.controller;


import com.asgard.user.entity.User;
import com.asgard.user.payload.request.CreateEditRequest;
import com.asgard.user.payload.request.CreateLoginRequest;
import com.asgard.user.payload.request.CreateUserRequest;
import com.asgard.user.payload.request.CreateUserRequestID;
import com.asgard.user.payload.request.RequestUserList;
import com.asgard.user.payload.response.ResponseRole;
import com.asgard.user.payload.response.ResponseUserDetailsAndRole;
import com.asgard.user.payload.response.ResponseUserRoleList;
import com.asgard.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
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
public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();


    @Before
    public void setup(){
        mockMvc = MockMvcBuilders.standaloneSetup(userController).setControllerAdvice(new ControllerAdvicer()).build();
    }

    @Test
    public void createdMember_withRequestCreate_ReturnUserID() throws Exception {
        String url = "/api/user/create" ;

        User user = new User();
        user.setUserID(17);

        CreateUserRequest req = new CreateUserRequest();
        req.setFullName("AAA");
        req.setEmail("AAA@ascendcorp.com");
        req.setPassword("123456789000");

        Mockito.when(userService.createUser(req)).thenReturn(user);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.userID", Matchers.is(17)));
    }

    @Test
    public void createdMember_withRequestCreate_shouldThrowExceptionDuplicateEmail() throws Exception {
        String url = "/api/user/create" ;

        User user = new User();
        user.setUserID(17);

        CreateUserRequest req = new CreateUserRequest();
        req.setFullName("AAA");
        req.setEmail("AAA@ascendcorp.com");
        req.setPassword("123456789000");

        Mockito.when(userService.findDuplicateEmail(req.getEmail())).thenThrow(new RuntimeException("Duplicated email"));
        Mockito.when(userService.createUser(req)).thenReturn(user);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("Duplicated email")));
    }

    @Test
    public void userLogin_withCreateLoginRequest_shouldReturnUserID() throws Exception {
        String url = "/api/user/login" ;

        User user = new User();
        user.setUserID(17);

        CreateLoginRequest req = new CreateLoginRequest();
        req.setEmail("Banana@ascendcorp.com");
        req.setPassword("123456789555");

        Mockito.when(userService.Login(req)).thenReturn(user.getUserID());

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get(url)
                .param("email", "Banana@ascendcorp.com")
                .param("password", "123456789555");

//        MvcResult mvcResult = mockMvc.perform(builder).andReturn();
//
//        String responseBase = mvcResult.getResponse().getContentAsString() ;
//
//        System.out.println(responseBase);


        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.userID", Matchers.is(17)));
    }

    @Test
    public void getUserList_withRequestUserList_should() throws Exception {
        String url = "/api/user/list?roleID=1" ;

        List<Integer> roleList = new ArrayList<>();
        roleList.add(1);

        RequestUserList req = new RequestUserList();
        req.setSearch("D");
        req.setRoleID(roleList);
        req.setPage(1);
        req.setPageSize(10);


        ResponseUserRoleList response = new ResponseUserRoleList();
        response.setUserID(17);
        response.setFullName("How");
        response.setEmail("How@ascendcorp.com");
        response.setTitle("admin");

        List<ResponseUserRoleList> resList = new ArrayList<>();

        resList.add(response);

        Mockito.when(userService.findUserList(req)).thenReturn(resList);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get(url)
                .param("search", req.getSearch())
                .param("page", req.getPage().toString())
                .param("pageSize", req.getPageSize().toString());


//        MvcResult mvcResult = mockMvc.perform(builder).andReturn();
//
//        String responseBase = mvcResult.getResponse().getContentAsString() ;
//
//        System.out.println(responseBase);

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.userID", Matchers.is(17)));
    }

    @Test
    public void getUserDetails_withUserID_shouldReturnResponseUserDetailsAndRole() throws Exception {
        String url = "/api/user/{id}/details" ;
        Integer id = 17 ;

        List<ResponseRole> responseRoles = new ArrayList<>();

        ResponseRole responseRole = new ResponseRole();
        responseRole.setRoleID(1);
        responseRole.setTitle("admin");

        responseRoles.add(responseRole);

        ResponseUserDetailsAndRole res = new ResponseUserDetailsAndRole();
        res.setUserID(17);
        res.setResponseRoleList(responseRoles);
        res.setFullName("AAA");
        res.setEmail("AAA@hotmail.com");

        Mockito.when(userService.findUserDetails(id)).thenReturn(res);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get(url, id);

//        MvcResult mvcResult = mockMvc.perform(builder).andReturn();
//
//        String responseBase = mvcResult.getResponse().getContentAsString() ;
//
//        System.out.println(responseBase);

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userID", Matchers.is(17)));

    }

    @Test
    public void getUserDetails_withUserID_shouldThrowException() throws Exception {
        String url = "/api/user/{id}/details" ;
        Integer id = 17 ;

        Mockito.when(userService.findUserDetails(id)).thenThrow(new RuntimeException("Not found user with role"));

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get(url, id);

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("Not found user with role")));
    }

    @Test
    public void editUser_withRequestEdit_shouldReturnUserID() throws Exception {
        String url = "/api/user/edit";
         Integer userID = 17;

        List<Integer> roleList = new ArrayList<>();
        roleList.add(1);
        roleList.add(2);

        CreateEditRequest req = new CreateEditRequest();
        req.setUserID(17);
        req.setFullName("BBB");
        req.setEmail("BBB@hotmail.com");
        req.setRoleIDList(roleList);

        Mockito.when(userService.editUserRole(req)).thenReturn(17);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .put(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.userID", Matchers.is(17)));
    }

    @Test
    public void editUser_withRequestEdit_shouldThrowException() throws Exception {
        String url = "/api/user/edit";
        Integer userID = 17;

        List<Integer> roleList = new ArrayList<>();
        roleList.add(1);
        roleList.add(2);

        CreateEditRequest req = new CreateEditRequest();
        req.setUserID(17);
        req.setFullName("BBB");
        req.setEmail("BBB@hotmail.com");
        req.setRoleIDList(roleList);

        Mockito.when(userService.editUserRole(req)).thenThrow(new RuntimeException("Duplicated email"));

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .put(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("Duplicated email")));
    }

    @Test
    public void deleteUser_withUserID_shouldThrowException() throws Exception {
        String url = "/api/user/delete" ;


        Integer userID = 17 ;

        CreateUserRequestID req = new CreateUserRequestID();
        req.setUserID(17);

        Mockito.when(userService.deleteUser(req.getUserID())).thenThrow(new RuntimeException("Not found this user"));

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .delete(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.is("Not found this user")));
    }

    @Test
    public void deleteUser_withUserID_shouldReturnUserID() throws Exception {
        String url = "/api/user/delete" ;


        Integer userID = 17 ;

        CreateUserRequestID req = new CreateUserRequestID();
        req.setUserID(17);

        Mockito.when(userService.deleteUser(req.getUserID())).thenReturn(userID);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .delete(url)
                .content(objectMapper.writeValueAsString(req))
                .contentType("application/json");

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.userID", Matchers.is(17)));
    }





}
