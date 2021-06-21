package com.upgrad.FoodOrderingApp.api.controller;


import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.common.FoodOrderingConstants;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.UUID;

@RestController
@CrossOrigin
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    /**
     *
     * @param signupUserRequest
     * @return
     * @throws SignUpRestrictedException
     *
     * Customer sign up controller mapping
     *
     */
    @RequestMapping(method = RequestMethod.POST, path = "/customer/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signup(@RequestBody final SignupCustomerRequest signupUserRequest) throws SignUpRestrictedException {

        //Lets do some validations
        if (signupUserRequest.getFirstName().isEmpty() ||
                signupUserRequest.getContactNumber().isEmpty() ||
                signupUserRequest.getEmailAddress().isEmpty() ||
                signupUserRequest.getPassword().isEmpty()) {
            throw new SignUpRestrictedException("SGR-005", "Except last name all fields should be filled");
        }
        final CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setUuid(UUID.randomUUID().toString());
        customerEntity.setFirstName(signupUserRequest.getFirstName());
        customerEntity.setLastName(signupUserRequest.getLastName());
        customerEntity.setEmail(signupUserRequest.getEmailAddress());
        customerEntity.setPassword(signupUserRequest.getPassword());
        //set a default salt value.
        customerEntity.setSalt("1234abc");
        customerEntity.setContactNumber(signupUserRequest.getContactNumber());
        final CustomerEntity createdUserEntity = customerService.saveCustomer(customerEntity);
        SignupCustomerResponse userResponse = new SignupCustomerResponse().id(createdUserEntity.getUuid()).status("CUSTOMER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }

    /**
     *
     * @param authorization
     * @return
     * @throws AuthenticationFailedException
     *
     * Login mapping for customer controller, log in using Basic Auth headers.
     *
     */

    @RequestMapping(method = RequestMethod.POST, path = "/customer/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException {

        //Extract auth token
        String[] authEncoded = authorization.split(FoodOrderingConstants.PREFIX_BASIC);
        String encoded = "";
        if (authEncoded.length > 1) {
            encoded = authEncoded[1];
        } else {
            throw new AuthenticationFailedException("ATH-003", "Incorrect format of decoded customer name and password");
        }
        byte[] decode;
        try {
            //extract username/password from the Base64 encoded token
            decode = Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException ex) {
            throw new AuthenticationFailedException("ATH-003", "Incorrect format of decoded customer name and password");
        }
        String decodedText = new String(decode);
        String[] decodedArray = decodedText.split(":");

        //who knows may be it might not work when splitting invalid format token
        String contact = "rather peculiar username";
        String passowrd = "and a strange password";

        if (decodedArray.length > 1) {
            contact = decodedArray[0];
            passowrd = decodedArray[1];
        } else {
            throw new AuthenticationFailedException("ATH-003", "Incorrect format of decoded customer name and password");
        }
        //Authenticate and set auth data in DB
        CustomerAuthEntity customerAuthEntity = customerService.authenticate(contact, passowrd);
        CustomerEntity user = customerAuthEntity.getCustomer();

        LoginResponse authorizedUserResponse = new LoginResponse().id(user.getUuid()).message("LOGGED IN SUCCESSFULLY").firstName(user.getFirstName()).lastName(user.getLastName()).contactNumber(user.getContactNumber()).emailAddress(user.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", customerAuthEntity.getAccessToken());
        return new ResponseEntity<LoginResponse>(authorizedUserResponse, headers, HttpStatus.OK);
    }

    /**
     *
     * @param authorization
     * @return
     * @throws AuthorizationFailedException
     *
     * Logout mapping for user controller, based on Bearer token
     *
     */


    @RequestMapping(method = RequestMethod.POST, path = "/customer/logout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LogoutResponse> logout(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {

        //Extract token and get auth entity from DB
        String[] authEncoded = authorization.split(FoodOrderingConstants.PREFIX_BEARER);
        String authToken = "";
        if (authEncoded.length > 1) {
            authToken = authEncoded[1];
        } else {
            authToken = "nonexistant";
        }
        //logout and update DB
        CustomerAuthEntity customerAuthEntity = customerService.logout(authToken);
        if (customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "This shouldnt get thrown");
        }
        CustomerEntity customerEntity = customerAuthEntity.getCustomer();
        LogoutResponse signoutResponse = new LogoutResponse().id(customerEntity.getUuid()).message("LOGGED OUT SUCCESSFULLY");

        return new ResponseEntity<>(signoutResponse, HttpStatus.OK);
    }

    /**
     *
     * @param authorization
     * @param updateCustomerRequest
     * @return
     * @throws UpdateCustomerException
     * @throws AuthorizationFailedException
     *
     * Update customer mapping for controller using PUT request
     *
     */
    @RequestMapping(method = RequestMethod.PUT, path = "/customer", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdateCustomerResponse> updateCustomer(@RequestHeader("authorization") final String authorization, @RequestBody final UpdateCustomerRequest updateCustomerRequest) throws UpdateCustomerException, AuthorizationFailedException {

        //Get the auth token

        String[] authEncoded = authorization.split(FoodOrderingConstants.PREFIX_BEARER);
        String authToken = "";
        if (authEncoded.length > 1) {
            authToken = authEncoded[1];
        } else {
            authToken = "nonexistant";
        }

        //necessary validation
        if (updateCustomerRequest.getFirstName().isEmpty()) {
            throw new UpdateCustomerException("UCR-002", "First name field should not be empty");
        }
        CustomerEntity customerEntity = customerService.getCustomer(authToken);
        customerEntity.setFirstName(updateCustomerRequest.getFirstName());
        customerEntity.setLastName(updateCustomerRequest.getLastName());
        final CustomerEntity updatedCustomer = customerService.updateCustomer(customerEntity);
        UpdateCustomerResponse userResponse = new UpdateCustomerResponse().id(updatedCustomer.getUuid()).firstName(updatedCustomer.getFirstName()).lastName(updatedCustomer.getLastName()).status("CUSTOMER SUCCESSFULLY UPDATED");
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    /**
     *
     * @param authorization
     * @param updatePasswordRequest
     * @return
     * @throws AuthorizationFailedException
     * @throws UpdateCustomerException
     *
     * Update password mapping for user controller
     *
     */
    @RequestMapping(method = RequestMethod.PUT, path = "/customer/password", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdatePasswordResponse> updatePassword(@RequestHeader("authorization") final String authorization, @RequestBody final UpdatePasswordRequest updatePasswordRequest) throws AuthorizationFailedException, UpdateCustomerException {

        //Lets do some validations
        if (updatePasswordRequest.getNewPassword().isEmpty() || updatePasswordRequest.getOldPassword().isEmpty()) {
            throw new UpdateCustomerException("UCR-003", "No field should be empty");
        }
        //Get auth token and hence the associated user.
        String[] authEncoded = authorization.split(FoodOrderingConstants.PREFIX_BEARER);
        String authToken = "";
        if (authEncoded.length > 1) {
            authToken = authEncoded[1];
        } else {
            authToken = "nonexistant";
        }
        CustomerEntity customerEntity = customerService.getCustomer(authToken);
        final CustomerEntity updatedCustomer = customerService.updateCustomerPassword(updatePasswordRequest.getOldPassword(), updatePasswordRequest.getNewPassword(), customerEntity);
        UpdatePasswordResponse userResponse = new UpdatePasswordResponse().id(updatedCustomer.getUuid()).status("CUSTOMER PASSWORD UPDATED SUCCESSFULLY");
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }
}