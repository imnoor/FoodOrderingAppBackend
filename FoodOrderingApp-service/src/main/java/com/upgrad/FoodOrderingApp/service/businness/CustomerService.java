package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class CustomerService {
    @Autowired
    private CustomerDao customerDAO;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;


    /**
     *
     * @param customerEntity
     * @return
     * @throws SignUpRestrictedException
     *
     * Register customer based on details provided
     *
     */
    @Transactional
    public CustomerEntity saveCustomer(final CustomerEntity customerEntity) throws SignUpRestrictedException {

        //Validate contact numer
        String regex = "^\\d{10}$";
        if (!customerEntity.getContactNumber().matches(regex)) {
            throw new SignUpRestrictedException("SGR-003", "Invalid contact number!");
        }
        //validate email
        regex = "^[a-zA-Z0-9]+@([a-zA-Z0-9]+\\.)+[a-zA-Z0-9]+$";
        if (!customerEntity.getEmail().matches(regex)) {
            throw new SignUpRestrictedException("SGR-002", "Invalid email-id format!");
        }

        //enforce password requirement
        regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\\\[#@$%&*!^\\\\] –[{}]:;',?\\/*~$^\\+=<>]).{8,20}$";
        if (!customerEntity.getPassword().matches(regex)) {
            throw new SignUpRestrictedException("SGR-004", "Weak password!");
        }

        if (this.contactExists(customerEntity.getContactNumber())) {
            throw new SignUpRestrictedException("SGR-001", "This contact number is already registered! Try other contact number.");
        }

        if (this.emailExists(customerEntity.getEmail())) {
            throw new SignUpRestrictedException("SGR-002", "This user has already been registered, try with any other emailId");
        }
        //Utility function to encrypt password to be stored in DB
        String[] encryptedText = cryptographyProvider.encrypt(customerEntity.getPassword());
        customerEntity.setSalt(encryptedText[0]);
        customerEntity.setPassword(encryptedText[1]);
        return customerDAO.createCustomer(customerEntity);
    }


    /**
     *
     * @param contact
     * @param password
     * @return
     * @throws AuthenticationFailedException
     *
     * Authenticate the user based on contact and password.
     *
     */
    @Transactional
    public CustomerAuthEntity authenticate(String contact, String password) throws AuthenticationFailedException {
        CustomerEntity customerEntity = customerDAO.getUserByContact(contact);
        if (!this.contactExists(contact)) {
            throw new AuthenticationFailedException("ATH-001", "This contact number has not been registered!");
        }
        final String encryptedPassword = cryptographyProvider.encrypt(password, customerEntity.getSalt());

        //If password matches, set session authentication details in  DB
        if (encryptedPassword.equals(customerEntity.getPassword())) {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            CustomerAuthEntity customerAuthEntity = new CustomerAuthEntity();
            customerAuthEntity.setCustomer(customerEntity);
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            customerAuthEntity.setAccessToken(jwtTokenProvider.generateToken(customerEntity.getUuid(), now, expiresAt));
            customerAuthEntity.setLoginAt(now);
            customerAuthEntity.setExpiresAt(expiresAt);
            customerAuthEntity.setUuid(UUID.randomUUID().toString());
            customerDAO.createAuthToken(customerAuthEntity);
            return customerAuthEntity;
        } else {
            throw new AuthenticationFailedException("ATH-002", "Invalid Credentials");
        }

    }

    /**
     *
     * @param email
     * @return
     *
     * Check existing emails
     *
     */
    public boolean emailExists(final String email) {
        return customerDAO.getUserByEmail(email) != null;
    }


    /**
     *
     * @param contact
     * @return
     *
     * Check existing contact
     *
     */
    public boolean contactExists(final String contact) {
        return customerDAO.getUserByContact(contact) != null;
    }

    /**
     *
     * @param authorization
     * @return
     *
     * Get Authentication info based on authorization token
     *
     */
    public CustomerAuthEntity getCustomerAccessToken(String authorization) {
        CustomerAuthEntity authEntity = customerDAO.getCustomerAuthToken(authorization);
        return authEntity;
    }

    /**
     *
     * @param customerEntity
     * @return
     *
     * Update customer details
     *
     */
    @Transactional
    public CustomerEntity updateCustomer(CustomerEntity customerEntity) {
        customerDAO.updateUser(customerEntity);
        return customerEntity;
    }

    /**
     *
     * @param authorizationToken
     * @return
     * @throws AuthorizationFailedException
     *
     * Facade for validating access token string
     *
     */
    public CustomerAuthEntity validateAccessToken(final String authorizationToken) throws AuthorizationFailedException {

        CustomerAuthEntity customerAuthTokenEntity = customerDAO.getCustomerAuthToken(authorizationToken);

        final ZonedDateTime now = ZonedDateTime.now();

        this.validateAccessTokenEntity(customerAuthTokenEntity);

        return customerAuthTokenEntity;
    }

    /**
     *
     * @param authEntity
     * @throws AuthorizationFailedException
     *
     * Private function for validating auth token entity
     *
     */
    private void validateAccessTokenEntity(CustomerAuthEntity authEntity) throws AuthorizationFailedException {

        //validate teh auth entity for authorizations
        final ZonedDateTime now = ZonedDateTime.now();
        if (authEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        } else if (authEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        } else if (now.isAfter(authEntity.getExpiresAt())) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }

    }

    /**
     *
     * @param customerEntity
     * @param password
     * @return
     *
     * Check customer entity and its password.
     *
     */
    public boolean checkPassword(CustomerEntity customerEntity, String password) {
        final String encryptedPassword = cryptographyProvider.encrypt(password, customerEntity.getSalt());
        if (encryptedPassword.equals(customerEntity.getPassword())) {
            return true;
        }
        return false;
    }


    /**
     *
     * @param oldPwd
     * @param newPwd
     * @param customerEntity
     * @return
     * @throws UpdateCustomerException
     *
     * Validate password and update.
     *
     */
    @Transactional
    public CustomerEntity updateCustomerPassword(String oldPwd, String newPwd, CustomerEntity customerEntity) throws UpdateCustomerException {

        //ensure password strength
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\\\[#@$%&*!^\\\\] –[{}]:;',?/*~$^+=<>]).{8,20}$";
        if (!newPwd.matches(regex)) {
            throw new UpdateCustomerException("UCR-001", "Weak password!");
        }
        if (!this.checkPassword(customerEntity, oldPwd)) {
            throw new UpdateCustomerException("UCR-004", "Incorrect old password!");
        }

        final String encryptedPassword = cryptographyProvider.encrypt(newPwd, customerEntity.getSalt());
        customerEntity.setPassword(encryptedPassword);
        return customerDAO.updateUser(customerEntity);
    }

    /**
     *
     * @param accessToken
     * @return
     * @throws AuthorizationFailedException
     *
     * Get customer based on authorization token without checking authorizations
     *
     */
    public CustomerEntity getCustomer(String accessToken) throws AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity = this.validateAccessToken(accessToken);
        if (customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "This should not trigger");
        }
        return customerAuthEntity.getCustomer();
    }


    /**
     *
     * @param authorization
     * @return
     * @throws AuthorizationFailedException
     *
     * Log the customer out based on the authorization token
     *
     */
    @Transactional
    public CustomerAuthEntity logout(String authorization) throws AuthorizationFailedException {
        CustomerAuthEntity authEntity = this.getCustomerAccessToken(authorization);
        this.validateAccessTokenEntity(authEntity);
        authEntity.setLogoutAt(ZonedDateTime.now());
        customerDAO.updateAuthToken(authEntity);
        return authEntity;
    }

    /**
     *
     * @param auth
     * @return
     * @throws AuthorizationFailedException
     *
     * Get customer based on authorization conditions
     *
     */
    public CustomerEntity getCustomerByAuthToken(String auth) throws AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity = customerDAO.getCustomerAuthToken(auth);

        //check authorization conditions
        this.validateAccessTokenEntity(customerAuthEntity);
        return customerAuthEntity.getCustomer();
    }
}
