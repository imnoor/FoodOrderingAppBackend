package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class RestaurantDao {
    @PersistenceContext
    EntityManager entityManager;

    /**
     *
     * @return
     */
    public List<RestaurantEntity> restaurantsByRating() {
        return entityManager.createNamedQuery("getAllRestaurants").getResultList();
    }

    /**
     *
     * @param name
     * @return
     */
    public List<RestaurantEntity> restaurantsByName(String name) {
        return entityManager.createNamedQuery("getRestaurantsByName").setParameter("name", "%" + name.toLowerCase() + "%").getResultList();
    }

    /**
     *
     * @param restaurantId
     * @return
     */
    public RestaurantEntity getRestaurantByID(String restaurantId) {
        try {
            return entityManager.createNamedQuery("getRestaurantById", RestaurantEntity.class).setParameter("id", restaurantId).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     *
     * @param categoryEntity
     * @return
     */
    public List<RestaurantEntity> restaurantByCategory(CategoryEntity categoryEntity) {
        try {
            return entityManager.createNamedQuery("getRestaurantByCategory", RestaurantEntity.class).setParameter("category", categoryEntity).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     *
     * @param restaurant
     * @return
     */
    public RestaurantEntity updateRestaurantRating(RestaurantEntity restaurant) {
        try {
            entityManager.merge(restaurant);
            return restaurant;
        } catch (NoResultException e) {
            return null;
        }
    }
}