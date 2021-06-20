//package com.upgrad.FoodOrderingApp.api.utils;
//
//import com.upgrad.FoodOrderingApp.api.model.RestaurantDetailsResponseAddress;
//import com.upgrad.FoodOrderingApp.api.model.RestaurantDetailsResponseAddressState;
//import com.upgrad.FoodOrderingApp.api.model.RestaurantList;
//import com.upgrad.FoodOrderingApp.service.businness.AddressService;
//import com.upgrad.FoodOrderingApp.service.businness.StateService;
//import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
//import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
//import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
//import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.UUID;
//
//public class RestaurantUtils {
//
//
//    @Autowired
//    private static AddressService addressService;
//
//    @Autowired
//    private static StateService stateService;
//
//    public static RestaurantList restaurantListTransformer(RestaurantEntity restaurantEntity) {
//        RestaurantList restaurantList = new RestaurantList();
//        restaurantList.setId(UUID.fromString(restaurantEntity.getUuid()));
//        restaurantList.setRestaurantName(restaurantEntity.getRestaurantName());
//        restaurantList.setPhotoURL(restaurantEntity.getPhotoUrl());
//        restaurantList.setCustomerRating(restaurantEntity.getCustomerRating());
//        restaurantList.setAveragePrice(restaurantEntity.getAvgPriceForTwo());
//        restaurantList.setNumberCustomersRated(restaurantEntity.getNumCustomersRated());
//
//        AddressEntity addressEntity = addressService.getAddressById(restaurantEntity.getAddress().getId());
//        RestaurantDetailsResponseAddress responseAddress = new RestaurantDetailsResponseAddress();
//
//        responseAddress.setId(UUID.fromString(addressEntity.getUuid()));
//        responseAddress.setFlatBuildingName(addressEntity.getFlatBuilNo());
//        responseAddress.setLocality(addressEntity.getLocality());
//        responseAddress.setCity(addressEntity.getCity());
//        responseAddress.setPincode(addressEntity.getPincode());
//
//        StateEntity stateEntity = stateService.getStateById(addressEntity.getState().getId());
//        RestaurantDetailsResponseAddressState responseAddressState = new RestaurantDetailsResponseAddressState();
//
//        responseAddressState.setId(UUID.fromString(stateEntity.getUuid()));
//        responseAddressState.setStateName(stateEntity.getStateName());
//        responseAddress.setState(responseAddressState);
//
//        restaurantList.setAddress(responseAddress);
//
//        List<String> categoryLists = new ArrayList();
//        for (CategoryEntity categoryEntity :restaurantEntity.getCategoryEntities()) {
//            categoryLists.add(categoryEntity.getCategoryName());
//        }
//
//        Collections.sort(categoryLists);
//
//        restaurantList.setCategories(String.join(",", categoryLists));
//        return restaurantList;
//    }
//}
