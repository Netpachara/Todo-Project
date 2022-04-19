package com.asgard.user.service;


import com.asgard.user.entity.Role;
import com.asgard.user.entity.User;
import com.asgard.user.entity.User_Role;
import com.asgard.user.entity.embeddedid.UserRoleId;
import com.asgard.user.payload.request.CreateEditRequest;
import com.asgard.user.payload.request.CreateUserRequest;
import com.asgard.user.payload.response.ResponseRole;
import com.asgard.user.payload.response.ResponseUserDetailsAndRole;
import com.asgard.user.repository.RoleRepository;
import com.asgard.user.repository.UserRepository;
import com.asgard.user.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private UserRepository userRepository;
    private UserRoleRepository userRoleRepository;
    private RoleRepository roleRepository;

    @Autowired
    public UserService(UserRepository u, UserRoleRepository ur, RoleRepository r){
        this.userRepository = u;
        this.userRoleRepository = ur;
        this.roleRepository = r;
    }

    public boolean findDuplicateEmail(String email){
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
        return roleRepository.findRoleID(id);
    }


    public ResponseUserDetailsAndRole findUserDetails(Integer id){
        List<User_Role> user_role = userRoleRepository.getUserDetails(id);
        User find_user = userRepository.findByUserID(id);
        List<Integer> roleIDList = new ArrayList<>();
        for(User_Role u : user_role){
            roleIDList.add(u.getUserRoleId().getRoleID());
        }
        List<Role> find_role = roleRepository.findRole(roleIDList);
        ResponseUserDetailsAndRole res = new ResponseUserDetailsAndRole();
        List<ResponseRole> responseRoleList = new ArrayList<>();
        for(Role r: find_role){
            ResponseRole responseRole = new ResponseRole();
            responseRole.setRoleID(r.getRoleID());
            responseRole.setTitle(r.getTitle());
            responseRoleList.add(responseRole);
            res.setResponseRoleList(responseRoleList);
        }
        res.setUserID(find_user.getUserID());
        res.setFullName(find_user.getFullName());
        res.setEmail(find_user.getEmail());
        return res;
    }

    public User createdUser(CreateUserRequest userDetails){
        User user = new User();
        user.setEmail(userDetails.getEmail());
        user.setFullName(userDetails.getFullName());
        user.setPassword(userDetails.getPassword());
        user = userRepository.save(user);

        UserRoleId userRoleId = new UserRoleId();
        userRoleId.setUserID(user.getUserID());
        userRoleId.setRoleID(2);
        User_Role user_role = new User_Role(userRoleId);
        userRoleRepository.save(user_role);

        return user;
    }

    @Transactional
    public Integer editUserRole(CreateEditRequest editRequest) throws Exception {
        if(userRepository.findByEmailANDUserID(editRequest.getEmail(), editRequest.getUserID()) != null){
            throw new Exception("Duplicated email");
        }

        User user = userRepository.findByUserID(editRequest.getUserID());
        user.setFullName(editRequest.getFullName());
        user.setEmail(editRequest.getEmail());
        userRepository.save(user);

        userRoleRepository.deleteByUserID(editRequest.getUserID());
        List<User_Role> user_roles = new ArrayList<>();
        for(Integer e : editRequest.getRoleIDList()){
            UserRoleId userRoleId = new UserRoleId();
            userRoleId.setUserID(editRequest.getUserID());
            userRoleId.setRoleID(e);
            user_roles.add(new User_Role(userRoleId));
        }
        userRoleRepository.saveAll(user_roles); // saveALL use with list (many element)
        return editRequest.getUserID();
    }

    public void deleteUser(Integer id){
        userRoleRepository.deleteByUserID(id);
        userRepository.deleteById(id);
    }

}
