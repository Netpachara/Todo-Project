package com.ascard.todoservice.service.Client;


import com.ascard.todoservice.payload.response.ResponseUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class UserClient {


    private RestTemplate restTemplate;
//    private String url = "http://localhost:8080";

    @Value("${client.user.hostname:http://localhost:8080}")
    private String url;


    @Autowired
    public UserClient(RestTemplate restTemplate){
        this.restTemplate = restTemplate ;
    }

    public ResponseUserDetails getUserDetails(Integer userID){
        try{
            System.out.println("Get User Details");
            System.out.println(url + "/api/user/{userID}/details");
            ResponseEntity<ResponseUserDetails> response = restTemplate.getForEntity(url+"/api/user/{userID}/details", ResponseUserDetails.class, userID);
            return response.getBody();
        }
        catch(Exception e){
            e.printStackTrace();
            return null ;
        }

    }

}
