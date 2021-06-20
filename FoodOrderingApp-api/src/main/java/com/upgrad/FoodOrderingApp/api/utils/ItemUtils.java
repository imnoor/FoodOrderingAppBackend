//package com.upgrad.FoodOrderingApp.api.utils;
//
//import com.upgrad.FoodOrderingApp.api.model.ItemList;
//import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//public class ItemUtils {
//    public static List<ItemList> serialiseItemList(List<ItemEntity> itemEntities) {
//        List<ItemList> itemLists = new ArrayList<ItemList>();
//        for (ItemEntity itemEntity :itemEntities) {
//            ItemList itemDetail = new ItemList();
//            itemDetail.setId(UUID.fromString(itemEntity.getUuid()));
//            itemDetail.setItemName(itemEntity.getItemName());
//            itemDetail.setPrice(itemEntity.getPrice());
//            itemDetail.setItemType(ItemList.ItemTypeEnum.fromValue(itemEntity.getType().name()));
//            itemLists.add(itemDetail);
//        }
//        return  itemLists;
//    }
//}
