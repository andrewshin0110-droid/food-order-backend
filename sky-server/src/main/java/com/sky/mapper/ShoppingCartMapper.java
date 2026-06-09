package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 動態條件查詢
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart  shoppingCart);

    /**
     * 根據id修改商品數量
     * @param shoppingCart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 插入購物車
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart(" +
            "name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time" +
            ") values (" +
            "#{name}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{image}, #{createTime}" +
            ")")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根據用戶id刪除購物車數據
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

    /**
     * 批量插入購物車
     * @param shoppingCartList
     */
    void insertBatch(List<ShoppingCart> shoppingCartList);

    /**
     * 根據id刪除購物車菜品
     * @param id
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);


}
