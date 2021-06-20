package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.StatesList;
import com.upgrad.FoodOrderingApp.api.model.StatesListResponse;
import com.upgrad.FoodOrderingApp.service.businness.AddressService;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
public class StatesController {
    @Autowired
    private AddressService addressService;

    @RequestMapping(method = RequestMethod.GET, path = "/states", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<StatesListResponse> getAllStates() {

        List<StateEntity> states = addressService.getAllStates();
        List<StatesList> stateList = new ArrayList<>();
        for (StateEntity stateEntity : states) {
            StatesList stateTmp = new StatesList();
            stateTmp.setId(UUID.fromString(stateEntity.getUuid()));
            stateTmp.setStateName(stateEntity.getStateName());
            stateList.add(stateTmp);
        }
        StatesListResponse response = new StatesListResponse();
        if (stateList.size() > 0) {
            response.setStates(stateList);
        }
        return new ResponseEntity<StatesListResponse>(response, HttpStatus.OK);
    }

}
