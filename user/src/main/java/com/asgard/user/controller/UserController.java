package com.asgard.user.controller;

import com.asgard.user.entity.User;
import com.asgard.user.payload.request.CreateLoginRequest;
import com.asgard.user.payload.request.CreateUserRequest;
import com.asgard.user.payload.response.CreateUserResponse;
import com.asgard.user.payload.response.ResponseBase;
import com.asgard.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class UserController {

    private UserService userService;
    private ControllerAdvicer controllerAdvicer;

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
//            responseBase.setErrors(controllerAdvicer.handleMethodArgumentNotValidException((MethodArgumentNotValidException) e));
        }
//        User id = userService.createdUser(userReq);
//        Response response = new Response();
//        response.textResult(userReq);
////        ResponseEntity response = new ResponseEntity(id, HttpStatus.OK);
//        return response.getDetails();
//            Map<String, Object> res = new HashMap<>();
//            res.put("data", createUserResponse);
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
//
//    @GetMapping("/user/list")
//    public void getUserList(){
//
//    }
//
//    @GetMapping("/user")
//    public void getUserDetails(){
//
//    }
//
//    @PutMapping("/user/{id}")
//    public void editUser(){
//
//    }

    @DeleteMapping("/user/{id}")
    public void deleteUser(@PathVariable Integer id){
        userService.deleteUser(id);
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
