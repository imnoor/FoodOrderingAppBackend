package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.businness.JwtTokenProvider;
import com.upgrad.FoodOrderingApp.service.businness.PasswordCryptographyProvider;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {
    @Autowired
    private CustomerDao customerDAO;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    @Transactional
    public CustomerEntity saveCustomer(final CustomerEntity customerEntity) throws SignUpRestrictedException {

        String regex = "^\\d{10}$";
        if (!customerEntity.getContactNumber().matches(regex)) {
            throw new SignUpRestrictedException("SGR-003", "Invalid contact number!");
        }

        regex = "^[a-zA-Z0-9]+@([a-zA-Z0-9]+\\.)+[a-zA-Z0-9]+$";
        if (!customerEntity.getEmail().matches(regex)) {
            throw new SignUpRestrictedException("SGR-002", "Invalid email-id format!");
        }

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
        String[] encryptedText = cryptographyProvider.encrypt(customerEntity.getPassword());
        customerEntity.setSalt(encryptedText[0]);
        customerEntity.setPassword(encryptedText[1]);
        return customerDAO.createCustomer(customerEntity);
    }

    @Transactional
    public CustomerAuthEntity authenticate(String contact, String password) throws AuthenticationFailedException {
        CustomerEntity customerEntity = customerDAO.getUserByContact(contact);
        if (!this.contactExists(contact)) {
            throw new AuthenticationFailedException("ATH-001", "This contact number has not been registered!");
        }
        final String encryptedPassword = cryptographyProvider.encrypt(password, customerEntity.getSalt());
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


    public boolean emailExists(final String email) {
        return customerDAO.getUserByEmail(email) != null;
    }

    public boolean contactExists(final String contact) {
        return customerDAO.getUserByContact(contact) != null;
    }

    public CustomerAuthEntity getCustomerAccessToken(String authorization) {
        CustomerAuthEntity authEntity = customerDAO.getCustomerAuthToken(authorization);
        return authEntity;
    }

    @Transactional
    public CustomerEntity updateCustomer(CustomerEntity customerEntity) {
        customerDAO.updateUser(customerEntity);
        return customerEntity;
    }

    public CustomerAuthEntity validateAccessToken(final String authorizationToken) throws AuthorizationFailedException {

        CustomerAuthEntity customerAuthTokenEntity = customerDAO.getCustomerAuthToken(authorizationToken);

        final ZonedDateTime now = ZonedDateTime.now();

        this.validateAccessTokenEntity(customerAuthTokenEntity);

        return customerAuthTokenEntity;
    }

    private void validateAccessTokenEntity(CustomerAuthEntity authEntity) throws AuthorizationFailedException {

        final ZonedDateTime now = ZonedDateTime.now();
        if (authEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        } else if (authEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        } else if (now.isAfter(authEntity.getExpiresAt())) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }

    }

    public boolean checkPassword(CustomerEntity customerEntity, String password) {
        final String encryptedPassword = cryptographyProvider.encrypt(password, customerEntity.getSalt());
        if (encryptedPassword.equals(customerEntity.getPassword())) {
            return true;
        }
        return false;
    }

    @Transactional
    public CustomerEntity updateCustomerPassword(String oldPwd, String newPwd, CustomerEntity customerEntity) throws UpdateCustomerException {


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

    public CustomerEntity getCustomer(String accessToken) throws AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity = this.validateAccessToken(accessToken);
        if (customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "This should not trigger");
        }
        return customerAuthEntity.getCustomer();
    }

    public CustomerEntity getCustomerByUUID(String Uuid) {
        return customerDAO.getUser(Uuid);
    }

    @Transactional
    public CustomerAuthEntity logout(String authorization) throws AuthorizationFailedException {
        CustomerAuthEntity authEntity = this.getCustomerAccessToken(authorization);
        this.validateAccessTokenEntity(authEntity);
        authEntity.setLogoutAt(ZonedDateTime.now());
        customerDAO.updateAuthToken(authEntity);
        return authEntity;
    }

    public CustomerEntity getCustomerByAuthToken(String auth) throws AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity = customerDAO.getCustomerAuthToken(auth);
        final ZonedDateTime now = ZonedDateTime.now();
        if (customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        } else if (customerAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        } else if (now.isAfter(customerAuthEntity.getExpiresAt())) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }
        this.validateAccessTokenEntity(customerAuthEntity);
        return customerAuthEntity.getCustomer();
    }
}
