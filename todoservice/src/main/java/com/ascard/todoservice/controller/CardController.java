package com.ascard.todoservice.controller;

import com.ascard.todoservice.payload.request.RequestCardList;
import com.ascard.todoservice.payload.request.RequestCreatedCard;
import com.ascard.todoservice.payload.request.RequestEditCardDetails;
import com.ascard.todoservice.payload.request.RequestUpdateCardStatus;
import com.ascard.todoservice.payload.response.ResponseBase;
import com.ascard.todoservice.payload.response.ResponseCardID;
import com.ascard.todoservice.payload.response.ResponseCardList;
import com.ascard.todoservice.payload.response.ResponseUserCardDetails;
import com.ascard.todoservice.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class CardController {

    private CardService cardService;

    @Autowired
    public CardController(CardService u){
        this.cardService = u;
    }

    @GetMapping("/api/card/list")
    public ResponseEntity getCardList(@Valid RequestCardList req){
        ResponseEntity response = null;
        ResponseBase responseBase = new ResponseBase();
        List<String> error = new ArrayList<>();
        try{
            ResponseCardList res = cardService.getCardList(req);
            responseBase.setData(res);
            response = new ResponseEntity(responseBase, HttpStatus.OK);
        }
        catch (Exception e){
            if(e.getMessage().equalsIgnoreCase("Card not found from search")){
                error.add(e.getMessage());
                responseBase.setErrors(error);
                response = new ResponseEntity(responseBase, HttpStatus.BAD_REQUEST);
            }
        }
        return response;
    }

    @GetMapping("/api/card/{id}/details")
    public ResponseEntity getCardDetails(@PathVariable("id") Integer cardId){
        ResponseEntity response= null;
        ResponseBase res = new ResponseBase();
        List<String> error = new ArrayList<>();
        try{
            ResponseUserCardDetails resDetails = cardService.getCardDetails(cardId);
            res.setData(resDetails);
            response = new ResponseEntity(res, HttpStatus.OK);
        }
        catch (Exception e){
            if(e.getMessage().equalsIgnoreCase("not found this cardID")){
                error.add(e.getMessage());
                res.setErrors(error);
                response = new ResponseEntity(res, HttpStatus.BAD_REQUEST);
            }
            else if(e.getMessage().equalsIgnoreCase("This card did not assigned")){
                error.add(e.getMessage());
                res.setErrors(error);
                response = new ResponseEntity(res, HttpStatus.BAD_REQUEST);
            }
            else if(e.getMessage().equalsIgnoreCase("Have not this userAssign in database")){
                error.add(e.getMessage());
                res.setErrors(error);
                response = new ResponseEntity(res, HttpStatus.BAD_REQUEST);
            }
            else if(e.getMessage().equalsIgnoreCase("Have not this userAssigned in database")){
                error.add(e.getMessage());
                res.setErrors(error);
                response = new ResponseEntity(res, HttpStatus.BAD_REQUEST);
            }
        }
        return response;
    }

    @PostMapping("/api/card/create")
    public ResponseEntity createdCard(@Valid @RequestBody RequestCreatedCard req){
        ResponseEntity response = null ;
        ResponseBase responseBase = new ResponseBase();
        List<String> error = new ArrayList<>();
        try{
            Integer res = cardService.createdCard(req);
            ResponseCardID responseCardID = new ResponseCardID();
            responseCardID.setCardID(res);
            responseBase.setData(responseCardID);
            response = new ResponseEntity(responseBase,HttpStatus.OK);
            return response;
        }
        catch (Exception e){
            if(e.getMessage().equalsIgnoreCase("You do not have permission")){
                error.add(e.getMessage());
                responseBase.setErrors(error);
                response = new ResponseEntity(responseBase, HttpStatus.BAD_REQUEST);
            }
            else if(e.getMessage().equalsIgnoreCase("This user is not admin")){
                error.add(e.getMessage());
                responseBase.setErrors(error);
                response = new ResponseEntity(responseBase, HttpStatus.BAD_REQUEST);
            }
            else if(e.getMessage().equalsIgnoreCase("This userAssigned is not in database")){
                error.add(e.getMessage());
                responseBase.setErrors(error);
                response = new ResponseEntity(responseBase, HttpStatus.BAD_REQUEST);
            }
            else{
                error.add(e.getMessage());
                responseBase.setErrors(error);
                response = new ResponseEntity(responseBase, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return response;
        }
    }


    @PutMapping("/api/card/edit")
    public ResponseEntity editCardDetails(@Valid @RequestBody RequestEditCardDetails req){
        ResponseEntity response = null ;
        ResponseBase responseBase = new ResponseBase();
        List<String> error = new ArrayList<>();
        try{
            Integer cardID = cardService.editCardDetails(req);
            ResponseCardID responseCardID = new ResponseCardID();
            responseCardID.setCardID(cardID);
            responseBase.setData(responseCardID);
            response = new ResponseEntity(responseBase, HttpStatus.OK);
        }
        catch (Exception e){
            if(e.getMessage().equalsIgnoreCase("you do not have permission")){
                error.add(e.getMessage());
                responseBase.setErrors(error);
                response = new ResponseEntity(responseBase, HttpStatus.BAD_REQUEST);
            }
            else if(e.getMessage().equalsIgnoreCase("this card not in database")){
                error.add(e.getMessage());
                responseBase.setErrors(error);
                response = new ResponseEntity(responseBase, HttpStatus.BAD_REQUEST);
            }
            else{
                error.add(e.getMessage());
                responseBase.setErrors(error);
                response = new ResponseEntity(responseBase, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return response ;
    }

    @PutMapping("/api/card/update")
    public ResponseEntity updateCardStatus(@Valid @RequestBody RequestUpdateCardStatus req){
        ResponseEntity response = null ;
        ResponseBase responseBase = new ResponseBase();
        List<String> error = new ArrayList<>();
        try{
            Integer cardID = cardService.updateCardStatus(req);
            ResponseCardID responseCardID = new ResponseCardID();
            responseCardID.setCardID(cardID);
            responseBase.setData(responseCardID);
            response = new ResponseEntity(responseBase, HttpStatus.OK);
        }
        catch(Exception e){
            if(e.getMessage().equalsIgnoreCase("This user is not admin")){
                error.add(e.getMessage());
                responseBase.setErrors(error);
                response = new ResponseEntity(responseBase, HttpStatus.BAD_REQUEST);
            }
            else if(e.getMessage().equalsIgnoreCase("error to update")){
                error.add(e.getMessage());
                responseBase.setErrors(error);
                response = new ResponseEntity(responseBase, HttpStatus.BAD_REQUEST);
            }
            else if(e.getMessage().equalsIgnoreCase("you do not have permission")){
                error.add(e.getMessage());
                responseBase.setErrors(error);
                response = new ResponseEntity(responseBase, HttpStatus.BAD_REQUEST);
            }
            else if(e.getMessage().equalsIgnoreCase("you do not have permission to edit this card")){
                error.add(e.getMessage());
                responseBase.setErrors(error);
                response = new ResponseEntity(responseBase, HttpStatus.BAD_REQUEST);
            }
            else if(e.getMessage().equalsIgnoreCase("this cardID is not in database")){
                error.add(e.getMessage());
                responseBase.setErrors(error);
                response = new ResponseEntity(responseBase, HttpStatus.BAD_REQUEST);
            }
            else{
                error.add(e.getMessage());
                responseBase.setErrors(error);
                response = new ResponseEntity(responseBase, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return response;
    }







}
