package com.asgard.user.controller;

import com.asgard.user.entity.User;
import com.asgard.user.payload.request.CreateEditRequest;
import com.asgard.user.payload.request.CreateLoginRequest;
import com.asgard.user.payload.request.CreateUserRequest;
import com.asgard.user.payload.request.CreateUserRequestID;
import com.asgard.user.payload.request.RequestUserList;
import com.asgard.user.payload.response.ResponseBase;
import com.asgard.user.payload.response.ResponseUserDetailsAndRole;
import com.asgard.user.payload.response.ResponseUserID;
import com.asgard.user.payload.response.ResponseUserList;
import com.asgard.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/api/user/created")
    public ResponseEntity<?> createdMember(@Valid @RequestBody CreateUserRequest userReq){
        ResponseBase responseBase = new ResponseBase();
        ResponseEntity<?> response = null;
        try{
            validateData(userReq);
            User user = userService.createdUser(userReq);
            ResponseUserID responseUserID = new ResponseUserID();
            responseUserID.setUserID(user.getUserID());
            responseBase.setData(responseUserID);
            response = new ResponseEntity<>(responseBase, HttpStatus.OK);
        }
        catch (Exception e){
            if (e.getMessage().equalsIgnoreCase("Duplicated email")) {
                response = new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }
        return response;
    }

    @GetMapping("/api/user/login")
    public ResponseEntity userLogin(@Valid @RequestBody CreateLoginRequest userLogin){
        User user = userService.Login(userLogin.getEmail(), userLogin.getPassword());
        ResponseUserID responseUserID = new ResponseUserID();
        responseUserID.setUserID(user.getUserID());
        ResponseEntity response = new ResponseEntity(responseUserID,HttpStatus.OK);
        return response;
    }

    @GetMapping("/api/user/list")
    public ResponseEntity getUserList(@Valid RequestUserList req) throws Exception {
        ResponseBase responseBase = new ResponseBase();
        ResponseEntity response = null ;
        List<ResponseUserList> res = new ArrayList<>();
        List<String> error = new ArrayList<>();
        try{
            List<ResponseUserList> user_role = userService.findUserList(req);

            for(ResponseUserList u : user_role){
                ResponseUserList rlt = new ResponseUserList() ;
                rlt.setUserID(u.getUserID());
                rlt.setFullName(u.getFullName());
                rlt.setEmail(u.getEmail());
//                rlt.setRoleID(userService.findRoleID(req.getRoleID()).getRoleID());
                rlt.setRoleID(u.getRoleID());
                rlt.setTitle(u.getTitle());
                res.add(rlt);
            }
            responseBase.setData(res);
            response = new ResponseEntity(responseBase,HttpStatus.OK);
        }
        catch (Exception e){
            if(e.getMessage().equalsIgnoreCase("Not found from search")){
                error.add(e.getMessage());
                responseBase.setErrors(error);
                response = new ResponseEntity(responseBase,HttpStatus.BAD_REQUEST);
            }
        }
        return response;
    }

    @GetMapping("/api/user/{id}/details")
    public ResponseEntity getUserDetails(@PathVariable("id") Integer userID){
        ResponseEntity response = null ;
        ResponseBase responseBase = new ResponseBase();
        List<String> error = new ArrayList<>();
        try{
            ResponseUserDetailsAndRole user_role = userService.findUserDetails(userID);
//            responseBase.setData(user_role);
            response = new ResponseEntity(user_role,HttpStatus.OK);
        }
        catch (Exception e){
            error.add(e.getMessage());
            responseBase.setErrors(error);
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBase);
        }
        return response;
    }


    @PutMapping("/api/user/edit")
    public ResponseEntity editUser(@Valid @RequestBody CreateEditRequest editRequest){
        ResponseEntity response = null;
        ResponseBase responseBase = new ResponseBase();
        List<String> error = new ArrayList<>();
        try{
            Integer userID = userService.editUserRole(editRequest);
            ResponseUserID responseUserID = new ResponseUserID();
            responseUserID.setUserID(userID);
            responseBase.setData(responseUserID);
            response = new ResponseEntity(responseBase,HttpStatus.OK);
        }
        catch (Exception e){
            if( e.getMessage().equalsIgnoreCase("Duplicated Email")){
                error.add(e.getMessage());
                responseBase.setErrors(error);
                response = new ResponseEntity(responseBase,HttpStatus.BAD_REQUEST);
            }
        }
        return response;
    }


    @DeleteMapping("/api/user/delete")
    public ResponseEntity deleteUser(@RequestBody CreateUserRequestID reqID){
        ResponseEntity response = null;
        List<String> error = new ArrayList<>();
        try{
            userService.deleteUser(reqID.getUserID());
            response = new ResponseEntity(HttpStatus.OK);
        }
        catch (Exception e){
            if(e.getMessage().equalsIgnoreCase("Not found this user")){
                error.add(e.getMessage());
            }
        }

        return response;
    }



    private void validateData(CreateUserRequest userReq) throws Exception {
        System.out.println(userService.findDuplicateEmail(userReq.getEmail()));
        if(userService.findDuplicateEmail(userReq.getEmail())){
            throw new Exception("Duplicated email");
        }
    }




}
