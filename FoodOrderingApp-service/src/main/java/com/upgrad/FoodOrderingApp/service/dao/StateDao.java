package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class StateDao {
    @PersistenceContext
    private EntityManager entityManager;

    /**
     *
     * @param stateId
     * @return
     */
    public StateEntity getStateById(final Long stateId) {
        try {
            return entityManager.createNamedQuery("stateById", StateEntity.class).setParameter("id", stateId)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     *
     * @param stateUuid
     * @return
     */
    public StateEntity getStateByUuid(String stateUuid) {
        try {
            return entityManager.createNamedQuery("stateByUuid", StateEntity.class).setParameter("uuid", stateUuid)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     *
     * @return
     */
    public List<StateEntity> getAllStates() {
        try {
            return entityManager.createNamedQuery("allStates", StateEntity.class)
                    .getResultList();
        } catch (NoResultException nre) {
            return null;
        }

    }
}
