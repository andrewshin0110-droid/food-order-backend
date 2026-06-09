package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.enumeration.OperationType;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入數據
     * @param orders
     */
    void insert(Orders orders);

    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime time);


    void update(Orders orders);

    /**
     * 根據id查詢訂單
     *
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 訂單搜索訂單搜索
     * @param ordersPageQueryDTO
     * @return
     */
    Page<OrderVO> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 各個狀態的訂單數量統計
     * @param status
     * @return
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);

    /**
     * 根據訂單號查詢訂單
     * @param orderNumber
     * @return
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);


    /**
     * 統計營業額
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 根據條件統計訂單數量
     * @param map
     */
    Integer countByMap(Map map);

    /**
     * 統計指定時間區間內的銷量排名前10
     * @param beginTime
     * @param endTime            
     * @return
     */
    List<GoodsSalesDTO> getSalseTop10(LocalDateTime beginTime, LocalDateTime endTime);
}
