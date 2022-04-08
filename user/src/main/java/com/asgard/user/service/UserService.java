package com.asgard.user.service;


import com.asgard.user.entity.User;
import com.asgard.user.payload.request.CreateUserRequest;
import com.asgard.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

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
        return false;

    }

    public User createdUser(CreateUserRequest userDetails){
        User user = new User();
        user.setEmail(userDetails.getEmail());
        user.setFullName(userDetails.getFullName());
        user.setPassword(userDetails.getPassword());
        user = userRepository.save(user);
        return user;
    }

}
