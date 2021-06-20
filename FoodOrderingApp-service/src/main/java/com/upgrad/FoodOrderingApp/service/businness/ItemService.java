package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CategoryDao;
import com.upgrad.FoodOrderingApp.service.dao.ItemDao;
import com.upgrad.FoodOrderingApp.service.dao.OrderDao;
import com.upgrad.FoodOrderingApp.service.dao.RestaurantDao;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.upgrad.FoodOrderingApp.service.common.GenericErrorCode.INF_001;

@Service
public class ItemService {

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private RestaurantDao restaurantDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private OrderDao orderDao;

    public List<ItemEntity> getItemsByCategoryAndRestaurant(String restaurantUuid, String categoryUuid) {
        RestaurantEntity restaurant = restaurantDao.getRestaurantByID(restaurantUuid);
        Set<ItemEntity> restaurantItems = restaurant.getItems();

        List<ItemEntity> filteredRestaurantItems = restaurantItems.stream().filter(restaurantItem ->
                restaurantItem.getCategories().stream()
                        .anyMatch(categoryEntity -> categoryEntity.getUuid().equals(categoryUuid)))
                .sorted(Comparator.comparing(ItemEntity::getItemName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        return filteredRestaurantItems;
    }

    public ItemEntity getItemById(String uuid) throws ItemNotFoundException {
        ItemEntity itemEntity = itemDao.getItemById(uuid);
        if (itemEntity == null) {
            throw new ItemNotFoundException(INF_001.getCode(), INF_001.getDefaultMessage());
        } else {
            return itemEntity;
        }
    }

    public List<ItemEntity> getItemsByPopularity(RestaurantEntity restaurantEntity) {

        List<ItemEntity> itemEntityList = new ArrayList<>();
        for (OrderEntity orderEntity : orderDao.getOrdersByRestaurant(restaurantEntity)) {
            orderEntity.getItems().forEach(items ->
                    itemEntityList.add(items.getItem())
            );
        }

        Map<String, Integer> unsortedItemCountMap = new HashMap<>();
        for (ItemEntity itemEntity : itemEntityList) {
            Integer count = unsortedItemCountMap.get(itemEntity.getUuid());
            unsortedItemCountMap.put(itemEntity.getUuid(), (count == null) ? 1 : count + 1);
        }

        List<Map.Entry<String, Integer>> unsortedItemCountList =
                new LinkedList<Map.Entry<String, Integer>>(unsortedItemCountMap.entrySet());

        unsortedItemCountList.sort(new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> item1,
                               Map.Entry<String, Integer> item2) {
                return (item2.getValue()).compareTo(item1.getValue());
            }
        });

        List<ItemEntity> sortedItemEntityList = new ArrayList<>();
        unsortedItemCountList.forEach(list ->
                sortedItemEntityList.add(itemDao.getItemById(list.getKey()))
        );

        return sortedItemEntityList;
    }
}
