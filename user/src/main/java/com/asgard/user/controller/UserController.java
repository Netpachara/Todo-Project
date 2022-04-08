package com.asgard.user.controller;

import com.asgard.user.entity.User;
import com.asgard.user.payload.request.CreateUserRequest;
import com.asgard.user.payload.response.CreateUserResponse;
import com.asgard.user.payload.response.ResponseBase;
import com.asgard.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService u){
        this.userService = u ;
    }

    @PostMapping("/user/created")
    public ResponseEntity<?> createdMember(@Valid @RequestBody CreateUserRequest userReq){
//        System.out.println("fullName: "+ userReq.getFullName());
//        System.out.println("Email: "+ userReq.getEmail());
//        System.out.println("Password: "+ userReq.getPassword());
        ResponseBase responseBase = new ResponseBase();
        try{
            validateData(userReq);
            User user = userService.createdUser(userReq);
            CreateUserResponse createUserResponse = new CreateUserResponse();
            createUserResponse.setUserID(user.getUserID());
            responseBase.setData(createUserResponse);
        }
        catch (Exception e){
            e.getMessage();
        }
//        User id = userService.createdUser(userReq);
//        Response response = new Response();
//        response.textResult(userReq);
////        ResponseEntity response = new ResponseEntity(id, HttpStatus.OK);
//        return response.getDetails();
//            Map<String, Object> res = new HashMap<>();
//            res.put("data", createUserResponse);
        ResponseEntity<?> response = new ResponseEntity<>(responseBase, HttpStatus.OK);
        return response;

    }

    private void validateData(CreateUserRequest userReq) throws Exception {
        System.out.println(userService.findDuplicateEmail(userReq.getEmail()));
        if(userService.findDuplicateEmail(userReq.getEmail())){
            System.out.println("Yes");
            System.out.println(userService.findDuplicateEmail(userReq.getEmail()));
            throw new Exception("Duplicated email");
        }
    }




}
