package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.PaymentEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class PaymentDao {

    @PersistenceContext
    EntityManager entityManager;

    /**
     *
     * @return
     */
    public List<PaymentEntity> getAllPaymentMethods() {
        return entityManager.createNamedQuery("getAllPaymentModes", PaymentEntity.class).getResultList();
    }

    /**
     *
     * @param uuid
     * @return
     */
    public PaymentEntity getPaymentByUUID(String uuid) {
        try {
            return entityManager.createNamedQuery("getPaymentModeById", PaymentEntity.class)
                    .setParameter("uuid", uuid)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

}
