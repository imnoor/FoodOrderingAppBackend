package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CouponEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class CouponDao {
    @PersistenceContext
    EntityManager entityManager;

    public CouponEntity getCouponByCouponName(final String couponName) {
        try {
            return entityManager.createNamedQuery("getCouponByName", CouponEntity.class)
                .setParameter("couponName", couponName)
                .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public CouponEntity getCouponByCouponId(String uuid) {
        try {
            return entityManager.createNamedQuery("getCouponByUuid", CouponEntity.class)
                .setParameter("uuid", uuid)
                .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
}
