package com.asgard.user.service;


import com.asgard.user.repository.UserInformationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private UserInformationRepository userInformationRepository;

    @Autowired
    public UserService(UserInformationRepository u){
        this.userInformationRepository = u;
    }



}
