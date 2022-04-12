package com.asgard.user.service;


import com.asgard.user.entity.Role;
import com.asgard.user.entity.User;
import com.asgard.user.entity.User_Role;
import com.asgard.user.payload.request.CreateEditRequest;
import com.asgard.user.payload.request.CreateUserRequest;
import com.asgard.user.payload.response.ResponseUserDetailsAndRole;
import com.asgard.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository u){
        this.userRepository = u;
    }

    public boolean findDuplicateEmail(String email){
        System.out.println("In FindDuplicated");
        if(userRepository.findByEmail(email) != null){
            return true;
        }
        if(userRepository.findByEmail(email) != null){
            return true;
        }
        return false;
    }

    public User Login(String email, String password){
        return userRepository.checkLogin(email, password);
    }

    public List<User> findUserList(String search, Integer roleID){
//        List<User> users = userRepository.findAll();
//        for(User u : users){
//            System.out.println(u.getUserID());
//            System.out.println(u.getUser_role().toString());
//            for(User_Role ur : u.getUser_role()){
//                System.out.println(ur.getRole());
//            }
//        }
        List<User> responseUserDetailsAndRole = userRepository.getUserList(search.toLowerCase(), roleID);

        return responseUserDetailsAndRole;

    }

    public Role findRoleID(Integer id){
        return userRepository.findRole(id);
    }

    public ResponseUserDetailsAndRole findUserDetails(Integer id){
        User_Role user_role = userRepository.getUserDetails(id);
        User find_user = userRepository.findByUserID(id);
        Role find_role = userRepository.findRole(user_role.getUserRoleId().getRoleID());
        ResponseUserDetailsAndRole res = new ResponseUserDetailsAndRole();
        res.setUserID(find_user.getUserID());
        res.setFullName(find_user.getFullName());
        res.setEmail(find_user.getEmail());
        res.setRoleID(find_role.getRoleID());
        res.setTitle(find_role.getTitle());
        return res;
    }

    public User createdUser(CreateUserRequest userDetails){
        User user = new User();
        user.setEmail(userDetails.getEmail());
        user.setFullName(userDetails.getFullName());
        user.setPassword(userDetails.getPassword());
        user = userRepository.save(user);
        return user;
    }

    public void editUserRole(CreateEditRequest editRequest){
        Optional<User> user = userRepository.findById(editRequest.getUserID());
//        user.map(UserInformation -> {
//            UserInformation.setFirstName(userDetails.getFirstName());
//            UserInformation.setLastName(userDetails.getLastName());
//            UserInformation.setEmail(userDetails.getEmail());
//            return userInformationRepository.save(UserInformation);
//        });
//        user.map(User -> {
//            User.set
//        });
    }

    public void deleteUser(Integer id){
        userRepository.deleteById(id);
    }

}
