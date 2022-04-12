package com.asgard.user.controller;

import com.asgard.user.entity.User;
import com.asgard.user.payload.request.CreateEditRequest;
import com.asgard.user.payload.request.CreateLoginRequest;
import com.asgard.user.payload.request.CreateUserRequest;
import com.asgard.user.payload.request.CreateUserRequestID;
import com.asgard.user.payload.response.CreateUserResponse;
import com.asgard.user.payload.response.ResponseBase;
import com.asgard.user.payload.response.ResponseUserDetailsAndRole;
import com.asgard.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService u){
        this.userService = u ;
    }

    @PostMapping("/user/created")
    public ResponseEntity<?> createdMember(@Valid @RequestBody CreateUserRequest userReq){
        ResponseBase responseBase = new ResponseBase();
        ResponseEntity<?> response = null;
        try{
            validateData(userReq);
            User user = userService.createdUser(userReq);
            CreateUserResponse createUserResponse = new CreateUserResponse();
            createUserResponse.setUserID(user.getUserID());
            responseBase.setData(createUserResponse);
            response = new ResponseEntity<>(responseBase, HttpStatus.OK);
        }
        catch (Exception e){
            if (e.getMessage().equalsIgnoreCase("Duplicated email")) {
                response = new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }
        return response;
    }

    @GetMapping("/user")
    public ResponseEntity userLogin(@Valid @RequestBody CreateLoginRequest userLogin){
        User user = userService.Login(userLogin.getEmail(), userLogin.getPassword());
        CreateUserResponse createUserResponse = new CreateUserResponse();
        createUserResponse.setUserID(user.getUserID());
        ResponseEntity response = new ResponseEntity(createUserResponse,HttpStatus.OK);
        return response;
    }

    @GetMapping("/user/list")
    public ResponseEntity getUserList(@Valid @Param("search") String search,@Valid @Param("roleID") Integer roleID){
        List<User> user_role = userService.findUserList(search, roleID);
        List<ResponseUserDetailsAndRole> res = new ArrayList<>();
        for(User u : user_role){
            ResponseUserDetailsAndRole response = new ResponseUserDetailsAndRole() ;
            response.setUserID(u.getUserID());
            response.setFullName(u.getFullName());
            response.setEmail(u.getEmail());
            response.setRoleID(userService.findRoleID(roleID).getRoleID());
            response.setTitle(userService.findRoleID(roleID).getTitle());
            res.add(response);
        }
        ResponseEntity response = new ResponseEntity(res,HttpStatus.OK);
        return response;
    }

    @GetMapping("/user/details")
    public ResponseEntity getUserDetails(@RequestBody CreateUserRequestID reqID){
        ResponseUserDetailsAndRole user_role = userService.findUserDetails(reqID.getUserID());
        ResponseEntity response = new ResponseEntity(user_role,HttpStatus.OK);
        return response;
    }


    @PutMapping("/user")
    public void editUser(@Valid @RequestBody CreateEditRequest editRequest){
        userService.editUserRole(editRequest);

    }


    @DeleteMapping("/user")
    public ResponseEntity deleteUser(@RequestBody CreateUserRequestID reqID){
        userService.deleteUser(reqID.getUserID());
        ResponseEntity response = new ResponseEntity(HttpStatus.OK);
        return response;
    }



    private void validateData(CreateUserRequest userReq) throws Exception {
        System.out.println(userService.findDuplicateEmail(userReq.getEmail()));
        if(userService.findDuplicateEmail(userReq.getEmail())){
            throw new Exception("Duplicated email");
        }
    }




}
