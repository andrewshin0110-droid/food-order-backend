package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    /**
     * 用戶下單
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 取消訂單
     * @param ordersCancelDTO
     */
    void cancelOrder(OrdersCancelDTO ordersCancelDTO);

    /**
     * 完成訂單
     * @param id
     */
    void completeOrder(Long id);

    /**
     * 接單
     * @param ordersConfirmDTO
     */
    void confirmOrder(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 拒單
     * @param ordersRejectionDTO
     */
    void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 查詢訂單詳情
     * @param id
     * @return
     */
    OrderVO detailOrder(Long id);

    /**
     * 派送訂單
     * @param id
     */
    void deliveryOrder(Long id);

    /**
     * 訂單搜索
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 各個狀態的訂單數量統計
     * @return
     */
    OrderStatisticsVO statisticsOrder();

    /**
     * 訂單支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO);

    void paySuccess(String outTradeNo);

    /**
     * 客戶催單
     * @param id
     */
    void remindById(Long id);

    /**
     * 歷史訂單查詢
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult pageQueryForUser(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 用戶取消訂單
     * @param id
     */
    void userCancelOrderById(Long id);

    /**
     * 再來一單
     * @param id
     */
    void repetitionById(Long id);
}
