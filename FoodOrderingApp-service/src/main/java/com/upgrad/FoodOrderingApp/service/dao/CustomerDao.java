package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class CustomerDao {
    @PersistenceContext
    private EntityManager entityManager;

    /**
     *
     * @param customerEntity
     * @return
     */
    public CustomerEntity createCustomer(CustomerEntity customerEntity) {
        entityManager.persist(customerEntity);
        return customerEntity;
    }

    /**
     *
     * @param userUuid
     * @return
     */
    public CustomerEntity getUser(final String userUuid) {
        try {
            return entityManager.createNamedQuery("userByUuid", CustomerEntity.class).setParameter("uuid", userUuid)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     *
     * @param contact
     * @return
     */
    public CustomerEntity getUserByContact(final String contact) {
        try {
            return entityManager.createNamedQuery("userByContact", CustomerEntity.class).setParameter("contactNumber", contact).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     *
     * @param email
     * @return
     */
    public CustomerEntity getUserByEmail(final String email) {
        try {
            return entityManager.createNamedQuery("userByEmail", CustomerEntity.class).setParameter("email", email).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     *
     * @param customerEntity
     * @return
     */
    public CustomerEntity updateUser(final CustomerEntity customerEntity) {
        return entityManager.merge(customerEntity);
    }

    /**
     *
     * @param delUser
     * @return
     */
    public String deleteUser(CustomerEntity delUser) {
        String uuid = delUser.getUuid();
        entityManager.remove(delUser);
        return uuid;
    }

    /**
     *
     * @param customerAuthEntity
     * @return
     */
    public CustomerAuthEntity createAuthToken(CustomerAuthEntity customerAuthEntity) {
        entityManager.persist(customerAuthEntity);
        return customerAuthEntity;
    }

    public CustomerAuthEntity getCustomerAuthToken(String authorization) {
        try {
            return entityManager.createNamedQuery("userAuthTokenByAccessToken", CustomerAuthEntity.class).setParameter("accessToken", authorization).getSingleResult();
        } catch (NoResultException nre) {

            return null;
        }
    }

    /**
     *
     * @param customerAuthEntity
     * @return
     */
    public CustomerAuthEntity updateAuthToken(CustomerAuthEntity customerAuthEntity) {
        return entityManager.merge(customerAuthEntity);
    }
}
