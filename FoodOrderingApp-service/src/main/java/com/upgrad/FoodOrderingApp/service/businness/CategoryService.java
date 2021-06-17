package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CategoryDao;
import com.upgrad.FoodOrderingApp.service.dao.RestaurantDao;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.upgrad.FoodOrderingApp.service.common.GenericErrorCode.CNF_001;
import static com.upgrad.FoodOrderingApp.service.common.GenericErrorCode.CNF_002;

@Service
public class CategoryService {

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private RestaurantDao restaurantDao;

    public List<CategoryEntity> getCategoriesByRestaurant(String restaurantUuid) {
        // Retrieve restaurantEntity from database
        RestaurantEntity restaurantEntity = restaurantDao.getRestaurantByID(restaurantUuid);
        // Retrieve CategoryEntity List from database
        return categoryDao.getCategoriesByRestaurant(restaurantEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<CategoryEntity> getAllCategoriesOrderedByName() {
        return categoryDao.getAllCategories();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CategoryEntity getCategoryById(String categoryId) throws CategoryNotFoundException {

        if (categoryId.equals("")) { // Throw error if categoryId is empty
            throw new CategoryNotFoundException(CNF_001.getCode(), CNF_001.getDefaultMessage());
        }

        // Retrieve categoryEntity from database
        CategoryEntity categoryEntity = categoryDao.getCategoryById(categoryId);

        if (categoryEntity == null) { // Throw error if category not found matching categoryId
            throw new CategoryNotFoundException(CNF_002.getCode(), CNF_002.getDefaultMessage());
        }

        return categoryEntity;
    }



}
