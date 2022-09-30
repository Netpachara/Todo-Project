package com.asgard.user.service;

import com.asgard.user.entity.Role;
import com.asgard.user.entity.User;
import com.asgard.user.entity.User_Role;
import com.asgard.user.entity.embeddedid.UserRoleId;
import com.asgard.user.payload.request.CreateEditRequest;
import com.asgard.user.payload.request.CreateUserRequest;
import com.asgard.user.payload.request.RequestUserList;
import com.asgard.user.payload.response.ResponseUserDetailsAndRole;
import com.asgard.user.payload.response.ResponseUserList;
import com.asgard.user.payload.response.ResponseUserRoleList;
import com.asgard.user.repository.RoleRepository;
import com.asgard.user.repository.UserListRepository;
import com.asgard.user.repository.UserRepository;
import com.asgard.user.repository.UserRoleRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;


@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService ;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserListRepository userListRepository;

    @Test
    public void findDuplicatedEmail_withEmail_ReturnTrue(){
        String email = "ascard@ascend.com" ;

        User user = new User();

        Mockito.when(userRepository.findByEmail(email)).thenReturn(user);

        Boolean validate = userService.findDuplicateEmail(email);

        Assert.assertEquals(validate, true);

    }

    @Test
    public void findDuplicatedEmail_withEmail_ReturnFalse(){
        String email = "AAA@ascend.com" ;

        Mockito.when(userRepository.findByEmail(email)).thenReturn(null);

        Boolean validate = userService.findDuplicateEmail(email);

        Assert.assertEquals(validate, false);

    }


    @Test
    public void whenFindUserList_withRequestUserList_shouldThrowExceptionNotFound() throws Exception {

        RequestUserList req = new RequestUserList();

        Mockito.when(userListRepository.getUserList(req)).thenReturn(null);

        try{
            List<ResponseUserRoleList> userList = userService.findUserList(req);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals("Not found from search", e.getMessage()); //expected
        }
    }



    @Test
    public void whenFindUserList_withRequestUserList_shouldReturnResponseUserRoleList() throws Exception {
        RequestUserList req = new RequestUserList();
        req.setSearch("D");

        UserRoleId userRoleId = new UserRoleId();
        userRoleId.setUserID(17);
        userRoleId.setRoleID(1);

        List<ResponseUserList> response = new ArrayList<>();

        ResponseUserList res = new ResponseUserList();
        res.setUserRoleId(userRoleId);
        res.setFullName("Chalalala");
        res.setEmail("Chalala@gmail.com");
        res.setTitle("admin");

        response.add(res);

        Mockito.when(userListRepository.getUserList(req)).thenReturn(response);

        List<ResponseUserRoleList> userList = userService.findUserList(req);

        Assert.assertEquals(17L, userList.get(0).getUserID().longValue());
        Assert.assertEquals("Chalalala", userList.get(0).getFullName());
        Assert.assertEquals("Chalala@gmail.com", userList.get(0).getEmail());
        Assert.assertEquals("admin", userList.get(0).getTitle());
    }


//    @Test
//    public void whenFindRoleID_withRoleID_shouldReturnRole(){
//
//        Role role = new Role();
//        role.setRoleID(1);
//        role.setTitle("admin");
//        Mockito.when(roleRepository.findRoleID(Mockito.anyInt())).thenReturn(role);
//
//        Assert.assertEquals();
//    }

    @Test
    public void whenFindUserDetails_withUserID_shouldThrowExceptionNotFoundUserWithRole() throws Exception {
        Integer id = 17 ;

        User_Role user_role = new User_Role();

        List<User_Role> user_roles = new ArrayList<>();

        Mockito.when(userRoleRepository.getUserDetails(id)).thenThrow(new RuntimeException("Not found user with role"));

        try{
            ResponseUserDetailsAndRole res = userService.findUserDetails(id);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals("Not found user with role", e.getMessage());
        }

    }


    @Test
    public void whenFindUserDetails_withUserID_shouldThrowExceptionUserNotInDatabase(){
        Integer id = 17;

        User_Role user_role = new User_Role();

        List<User_Role> user_roles = new ArrayList<>();
        user_roles.add(user_role);

        Mockito.when(userRoleRepository.getUserDetails(id)).thenReturn(user_roles);
        Mockito.when(userRepository.findByUserID(id)).thenReturn(null);

        try{
            ResponseUserDetailsAndRole res = userService.findUserDetails(id);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals("This userId is not in database", e.getMessage());
        }
    }

    @Test
    public void whenFindUserDetails_withUserID_shouldThrowExceptionRoleNotFound(){
        Integer id = 17;

        UserRoleId userRoleId = new UserRoleId();

        User_Role user_role = new User_Role();
        user_role.setUserRoleId(userRoleId);

        User user = new User();

        List<User_Role> user_roles = new ArrayList<>();
        user_roles.add(user_role);

        List<Integer> roleList = new ArrayList<>();

        Mockito.when(userRoleRepository.getUserDetails(id)).thenReturn(user_roles);
        Mockito.when(userRepository.findByUserID(id)).thenReturn(user);
        Mockito.when(roleRepository.findRole(roleList)).thenReturn(null);

        try{
            ResponseUserDetailsAndRole res = userService.findUserDetails(id);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals("Role not found", e.getMessage());
        }
    }

    @Test
    public void whenFindUserDetails_withUserID_shouldReturnUserDetailsAndRole(){
        Integer id = 17;

        UserRoleId userRoleId = new UserRoleId();

        User_Role user_role = new User_Role();
        user_role.setUserRoleId(userRoleId);

        User user = new User();
        user.setUserID(17);
        user.setFullName("AIA");
        user.setEmail("AIA@GGG.com");

        List<User_Role> user_roles = new ArrayList<>();
        user_roles.add(user_role);

        List<Integer> roleList = new ArrayList<>();
        roleList.add(1);

        List<Role> roles = new ArrayList<>();

        Role role = new Role();
        role.setRoleID(1);
        role.setTitle("admin");

        roles.add(role);

        Mockito.when(userRoleRepository.getUserDetails(id)).thenReturn(user_roles);
        Mockito.when(userRepository.findByUserID(id)).thenReturn(user);
        Mockito.when(roleRepository.findRole(roleList)).thenReturn(roles);

        try{
            ResponseUserDetailsAndRole res = userService.findUserDetails(id);
            Assert.assertEquals(17L, res.getUserID().longValue());
            Assert.assertEquals("AIA", res.getFullName());
            Assert.assertEquals("AIA@GGG.com", res.getEmail());
            Assert.assertNotNull(res.getResponseRoleList());
        }
        catch (Exception e){
            Assert.assertEquals("Role not found", e.getMessage());
        }
    }

    @Test
    public void whenCreateUser_withUserDetails_shouldReturnUserID(){

        CreateUserRequest req = new CreateUserRequest();
        req.setFullName("Initiater");
        req.setEmail("Initiater@gmail.com");
        req.setPassword("1234567890");

        User new_user = new User();
        new_user.setUserID(23);
        new_user.setFullName(req.getFullName());
        new_user.setEmail(req.getEmail());
        new_user.setPassword(req.getPassword());

        UserRoleId userRoleId = new UserRoleId();
        userRoleId.setRoleID(1);
        userRoleId.setUserID(17);

        User_Role user_role = new User_Role();
        user_role.setUserRoleId(userRoleId);

        Mockito.when(userRepository.save(new_user)).thenReturn(new_user);
        Mockito.when(userRoleRepository.save(user_role)).thenReturn(null);

        User user = userService.createUser(req);

        Assert.assertEquals("Initiater", user.getFullName());
        Assert.assertEquals("Initiater@gmail.com", user.getEmail());
        Assert.assertEquals("1234567890", user.getPassword());

    }

    @Test
    public void whenEditUserRole_withCreateEditRequest_shouldThrowExceptionDuplicatedEmail() throws Exception {
        CreateEditRequest req = new CreateEditRequest();
        req.setUserID(17);
        req.setFullName("Net");
        req.setEmail("Net@hotmail.com");

        List<Integer> roleList = new ArrayList<>();
        roleList.add(1);
        roleList.add(2);
        req.setRoleIDList(roleList);

        User user = new User();

        Mockito.when(userRepository.findByEmailANDUserID(req.getEmail(), req.getUserID())).thenReturn(user);

        try{
            Integer userID = userService.editUserRole(req);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals("Duplicated email", e.getMessage());
        }

    }

    @Test
    public void whenEditUserRole_withCreateEditRequest_shouldThrowExceptionNotFoundUser() throws Exception {
        CreateEditRequest req = new CreateEditRequest();
        req.setUserID(17);
        req.setFullName("Net");
        req.setEmail("Net@hotmail.com");

        List<Integer> roleList = new ArrayList<>();
        roleList.add(1);
        roleList.add(2);
        req.setRoleIDList(roleList);

        User user = new User();

        Mockito.when(userRepository.findByEmailANDUserID(req.getEmail(), req.getUserID())).thenReturn(null);
        Mockito.when(userRepository.findByUserID(req.getUserID())).thenReturn(null);

        try{
            Integer userID = userService.editUserRole(req);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals("Not found user", e.getMessage());
        }

    }

    @Test
    public void whenEditUserRole_withCreateEditRequest_shouldReturnUserID() throws Exception {
        CreateEditRequest req = new CreateEditRequest();
        req.setUserID(17);
        req.setFullName("Net");
        req.setEmail("Net@hotmail.com");

        List<Integer> roleList = new ArrayList<>();
        roleList.add(1);
        roleList.add(2);
        req.setRoleIDList(roleList);

        User user = new User();
        user.setUserID(17);


        Mockito.when(userRepository.findByEmailANDUserID(req.getEmail(), req.getUserID())).thenReturn(null);
        Mockito.when(userRepository.findByUserID(req.getUserID())).thenReturn(user);

        try{
            Integer userID = userService.editUserRole(req);
            Assert.assertEquals(17L, userID.longValue());
        }
        catch (Exception e){
            Assert.assertEquals("Not found user", e.getMessage());
        }

    }


    @Test
    public void whenDeleteUser_withUserID_shouldThrowExceptionNotFoundUser(){

        Integer id = 17;

        Mockito.when(userRepository.findByUserID(17)).thenReturn(null);

        try{
            userService.deleteUser(id);
            Assert.fail();
        }
        catch (Exception e){
            Assert.assertEquals("Not found this user", e.getMessage());
        }
    }

    @Test
    public void whenDeleteUser_withUserID_shouldNotThrowAnyException(){

        Integer id = 17;

        User user = new User();

        Mockito.when(userRepository.findByUserID(17)).thenReturn(user);

        try{
            userService.deleteUser(id);
        }
        catch (Exception e){
            Assert.assertEquals("Not found this user", e.getMessage());
        }
    }













}
