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

    public List<RestaurantEntity> restaurantsByRating() {
        return entityManager.createNamedQuery("getAllRestaurants").getResultList();
    }

    public List<RestaurantEntity> restaurantsByName(String name) {
        return entityManager.createNamedQuery("getRestaurantsByName").setParameter("name", "%" + name.toLowerCase() + "%").getResultList();
    }

    public RestaurantEntity getRestaurantByID(String restaurantId) {
        try {
            return entityManager.createNamedQuery("getRestaurantById", RestaurantEntity.class).setParameter("id", restaurantId).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<RestaurantEntity> restaurantByCategory(CategoryEntity categoryEntity) {
        try {
            return entityManager.createNamedQuery("getRestaurantByCategory", RestaurantEntity.class).setParameter("category", categoryEntity).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public RestaurantEntity updateRestaurantRating(RestaurantEntity restaurant) {
        try {
            entityManager.merge(restaurant);
            return restaurant;
        } catch (NoResultException e) {
            return null;
        }
    }
}