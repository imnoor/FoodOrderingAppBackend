package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.AddressService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.common.FoodOrderingConstants;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
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
public class AddressController {
    @Autowired
    private AddressService addressService;

    @Autowired
    private CustomerService customerService;

    /**
     *
     * @param authorization
     * @param saveAddressRequest
     * @return
     * @throws AuthorizationFailedException
     * @throws SaveAddressException
     * @throws AddressNotFoundException
     *
     * The controller method for adding new address
     *
     */
    @RequestMapping(method = RequestMethod.POST, path = "/address", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveAddressResponse> saveAddress(@RequestHeader("authorization") final String authorization, @RequestBody(required = false) final SaveAddressRequest saveAddressRequest) throws AuthorizationFailedException, SaveAddressException, AddressNotFoundException {

        //Retrieve auth token from the authorization header
        String[] authEncoded = authorization.split(FoodOrderingConstants.PREFIX_BEARER);
        String authToken = "";
        if (authEncoded.length > 1) {
            authToken = authEncoded[1];
        } else {
            authToken = "nonexistant";
        }
        //get customer entity based on the auth token
        CustomerEntity customerEntity = customerService.getCustomerByAuthToken(authToken);
        StateEntity stateEntity = addressService.getStateByUUID(saveAddressRequest.getStateUuid());

        //Set entity values based on request
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setFlatBuilNo(saveAddressRequest.getFlatBuildingName());
        addressEntity.setLocality(saveAddressRequest.getLocality());
        addressEntity.setCity(saveAddressRequest.getCity());
        addressEntity.setPincode(saveAddressRequest.getPincode());
        addressEntity.setState(stateEntity);
        addressEntity.setUuid(UUID.randomUUID().toString());
        addressEntity = addressService.saveAddress(addressEntity, customerEntity);
        SaveAddressResponse saveAddressResponse = new SaveAddressResponse().id(addressEntity.getUuid()).status("ADDRESS SUCCESSFULLY REGISTERED");
        return new ResponseEntity<>(saveAddressResponse, HttpStatus.CREATED);
    }

    /**
     *
     * @param authorization
     * @return
     * @throws AuthorizationFailedException
     *
     * Retrieve addresses of customer
     */

    @RequestMapping(method = RequestMethod.GET, path = "/address/customer", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AddressListResponse> getAllSavedAddresses(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
        //Retrieve auth token from the authorization header
        String[] authEncoded = authorization.split(FoodOrderingConstants.PREFIX_BEARER);
        String authToken = "";
        if (authEncoded.length > 1) {
            authToken = authEncoded[1];
        } else {

            authToken = "nonexistant";
        }
        //get customer for auth token
        CustomerEntity customerEntity = customerService.getCustomerByAuthToken(authToken);
        customerService.validateAccessToken(authToken);
        List<AddressEntity> addressEntities = addressService.getAllAddress(customerEntity);
        //Iterate and populate address list response
        List<AddressList> addressList = new ArrayList<>();
        for (AddressEntity addressEntity : addressEntities) {
            AddressList addTmp = new AddressList();
            addTmp.setId(UUID.fromString(addressEntity.getUuid()));
            addTmp.setFlatBuildingName(addressEntity.getFlatBuilNo());
            addTmp.locality(addressEntity.getLocality());
            addTmp.city(addressEntity.getCity());
            addTmp.pincode(addressEntity.getPincode());
            AddressListState state = new AddressListState();
            state.id(UUID.fromString(addressEntity.getState().getUuid())).stateName(addressEntity.getState().getStateName());
            addTmp.setState(state);
            addressList.add(addTmp);
        }
        AddressListResponse response = new AddressListResponse();
        response.setAddresses(addressList);
        return new ResponseEntity<AddressListResponse>(response, HttpStatus.OK);
    }

    /**
     *
     * @param addressUuid
     * @param authorization
     * @return
     * @throws AuthorizationFailedException
     * @throws AddressNotFoundException
     *
     * Delete address of customer based on auth token and address UUID
     *
     */

    @RequestMapping(method = RequestMethod.DELETE, path = "/address/{address_id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DeleteAddressResponse> deleteSavedAddress(@PathVariable("address_id") String addressUuid, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AddressNotFoundException {
        String[] authEncoded = authorization.split(FoodOrderingConstants.PREFIX_BEARER);
        String authToken = "";
        if (authEncoded.length > 1) {
            authToken = authEncoded[1];
        } else {
            authToken = "nonexistant";
        }
        CustomerEntity customerEntity = customerService.getCustomerByAuthToken(authToken);

        AddressEntity addressEntity = addressService.getAddressByUUID(addressUuid, customerEntity);
        //verify if address UUID is empty or not
        if (addressUuid.isEmpty()) {
            throw new AddressNotFoundException("ANF-005", "Address id can not be empty");
        }

        UUID retUUID = UUID.fromString(addressService.deleteAddress(addressEntity).getUuid());
        DeleteAddressResponse deleteAddressResponse = new DeleteAddressResponse().id(retUUID).status("ADDRESS DELETED SUCCESSFULLY");
        return new ResponseEntity<DeleteAddressResponse>(deleteAddressResponse, HttpStatus.OK);
    }

}