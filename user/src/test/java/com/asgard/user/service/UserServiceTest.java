//package com.asgard.user.service;
//
//import com.asgard.user.entity.Role;
//import com.asgard.user.entity.User;
//import com.asgard.user.entity.User_Role;
//import com.asgard.user.payload.request.CreateUserRequest;
//import com.asgard.user.repository.RoleRepository;
//import com.asgard.user.repository.UserRepository;
//import com.asgard.user.repository.UserRoleRepository;
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
//@RunWith(MockitoJUnitRunner.class)
//public class UserServiceTest {
//
//    @InjectMocks
//    private UserService userService ;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private UserRoleRepository userRoleRepository;
//
//    @Mock
//    private RoleRepository roleRepository;
//
//
//
//
//    @Test
//    public void whenFindUserList_withSearchRoleID_shouldReturnEmpty(){
//        List<User> user = new ArrayList<>();
//
//        Mockito.when(userRepository.getUserList("z", 1)).thenReturn(user);
//
//        List<User> userList = userService.findUserList("z", 1);
//
//        Assert.assertTrue(userList.isEmpty()); //expected
//
//    }
//
//    @Test
//    public void whenFindUserList_withSearchRoleID_shouldReturnNotEmpty(){
//        List<User> user = new ArrayList<>();
//
//        User newUser = new User();
//        newUser.setUserID(1);
//        newUser.setFullName("ABCDE");
//        newUser.setEmail("Bana@ascendcorp.com");
//        newUser.setPassword("123456789");
//        user.add(newUser);
//
//        Mockito.when(userRepository.getUserList(Mockito.anyString(), Mockito.eq(1))).thenReturn(user);
//
//        List<User> userList = userService.findUserList("b", 1);
//
//        Assert.assertFalse(userList.isEmpty()); //expected
//
//        Assert.assertEquals("ABCDE", userList.get(0).getFullName());
//    }
//
//    @Test
//    public void whenFindUserList_withSearchRoleID_shouldFoundThrowException(){
//
//        Mockito.when(userRepository.getUserList(Mockito.anyString(), Mockito.anyInt())).thenThrow(new Exception("Found"));
//
//        List<User> userList = userService.findUserList("b", 1);
//
//    }
//
//    @Test
//    public void whenFindRoleID_withRoleID_shouldReturnRole(){
//
//        Role role = new Role();
//        role.setRoleID(1);
//        role.setTitle("admin");
//        Mockito.when(roleRepository.findRoleID(Mockito.anyInt())).thenReturn(role);
//
////        Assert.assertEquals();
//    }
//
//    @Test
//    public void whenFindUserDetails_withUserID_shouldReturnNotEmpty(){
//        User_Role user_role = new User_Role();
//    }
//
//    @Test
//    public void whenFindUserDetails_withUserID_shouldReturnEmpty(){
//        User_Role user_role = new User_Role();
//    }
//
//    @Test
//    public void whenCreateUser_withUserDetails_shouldReturnUserID(){
//
//        User new_user = new User();
//        new_user.setUserID(23);
//        new_user.setFullName("Initiater");
//        new_user.setEmail("Initiater@gmail.com");
//        new_user.setPassword("1234567890");
//
//        CreateUserRequest createUserRequest = new CreateUserRequest();
//        createUserRequest.setFullName("Initiater");
//        createUserRequest.setEmail("Initiater@gmail.com");
//        createUserRequest.setPassword("1234567890");
//
//        Mockito.when(userRepository.save(new_user)).thenReturn(new_user);
//
//        User user = userService.createdUser(createUserRequest);
//
//        Assert.assertEquals("Initiater", user.getFullName());
//        Assert.assertEquals("Initiater@gmail.com", user.getEmail());
//        Assert.assertEquals("1234567890", user.getPassword());
//
//
//    }
//
//    @Test
//    public void whenEditUserRole_with_shouldReturnSomething(){
//
//    }
//
//
//
//
//
//
//
//
//
//
//
//}
