package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {


    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private WebSocketServer  webSocketServer;

    /**
     *用戶下單
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //處理各種業務異常(用戶地址為空 購物車數據為空)
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            //拋出業務異常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }


        //檢查當前用戶購物車數據
        Long usserId = BaseContext.getCurrentId();

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(usserId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        if(shoppingCartList == null || shoppingCartList.size() == 0){
            //拋出業務異常
            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向訂單表插入一條數據
        Orders orders = new Orders();
        //獲取地址字符串
        String orderAddress = getOrderAddressStr(ordersSubmitDTO);
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(orderAddress);
        orders.setUserId(usserId);

        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        //向訂單明細表插入n調數據
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();//訂單明細
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());//設置當前訂單明細關聯的訂單id
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);


        //清空當前用戶購物車數據
        shoppingCartMapper.deleteByUserId(usserId);

        //封裝成VO返回結果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

        return orderSubmitVO;
    }

    /**
     * 取消訂單
     * @param ordersCancelDTO
     */
    public void cancelOrder(OrdersCancelDTO ordersCancelDTO) {
        //根據ID查詢訂單
        Orders order = orderMapper.getById(ordersCancelDTO.getId());

        //查詢訂單支付狀態
        Integer payStatus = order.getPayStatus();
        if (payStatus == 1) {
            //如果已經付款 需退款給客戶 //TODO

        }

        //根据訂單id更新訂單的狀態 取消原因 取消時間
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersCancelDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setStatus(Orders.CANCELLED);
        orderMapper.update(orders);


    }

    /**
     * 完成訂單
     * @param id
     */
    public void completeOrder(Long id) {
        //根據訂單ID查詢訂單
        Orders order = orderMapper.getById(id);

        //較驗訂單是否存在 且狀態為4
        if (order == null || order.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //更新訂單狀態
        Orders orders = new Orders();
        orders.setId(order.getId());
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 接單
     * @param ordersConfirmDTO
     */
    public void confirmOrder(OrdersConfirmDTO ordersConfirmDTO) {
        Orders order = new Orders();
        order.setId(ordersConfirmDTO.getId());
        order.setStatus(Orders.CONFIRMED);
        orderMapper.update(order);

    }

    /**
     * 拒單
     * @param ordersRejectionDTO
     */
    public void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());

        //只有在訂單存在 和 訂單在待接單的狀態才可以拒單
        if (orders == null || orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //如果客戶已經付款 需要退款
        if(orders.getPayStatus() == Orders.PAID){
            //如果已經付款 需退款給客戶 //TODO
        }

        Orders order = new Orders();
        order.setId(orders.getId());
        order.setCancelReason(ordersRejectionDTO.getRejectionReason());
        order.setStatus(Orders.CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    /**
     * 查詢訂單詳情
     * @param id
     * @return
     */
    public OrderVO detailOrder(Long id) {
        Orders order = orderMapper.getById(id);

        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        List<OrderDetail> orderDetails = orderDetailMapper.getOrderDetailById(order.getId());

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    /**
     * 派送訂單
     * @param id
     */
    public void deliveryOrder(Long id) {
        Orders order = orderMapper.getById(id);

        if (order == null || !order.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(order.getId());
        orders.setDeliveryTime(LocalDateTime.now());
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

    /**
     * 訂單搜索
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<OrderVO> page = orderMapper.pageQuery(ordersPageQueryDTO);
        long total = page.getTotal();
        List<OrderVO> orderVOList = getOrderVOlist(page);
        return new PageResult(total, orderVOList);
    }

    /**
     * 各個狀態的訂單數量統計
     * @return
     */
    public OrderStatisticsVO statisticsOrder() {
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        Integer cancelled = orderMapper.countStatus(Orders.CANCELLED);
        Integer completed = orderMapper.countStatus(Orders.COMPLETED);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);



        return orderStatisticsVO;
    }

    /**
     * 訂單支付
     * @param ordersPaymentDTO
     * @return
     */
    @Override
    @Transactional
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {

        paySuccess(ordersPaymentDTO.getOrderNumber());

        OrderPaymentVO vo = new OrderPaymentVO();
        vo.setNonceStr("mock_nonce");
        vo.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
        vo.setPaySign("mock_sign");
        vo.setSignType("MockPay");
        return vo;
    }

    /**
     * 訂單支付狀態
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 已支付直接返回
        if (ordersDB.getPayStatus() == Orders.PAID) {
            return;
        }

        // 已取消不能支付
        if (ordersDB.getStatus() == Orders.CANCELLED) {
            throw new OrderBusinessException("訂單已取消，不能支付");
        }

        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        //透過webSocket向客戶端瀏覽器推送消息
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content", "訂單號" + outTradeNo);
        map.put("message", "有新的支付定單");

        webSocketServer.sendToAllClient(JSON.toJSONString(map));


    }

    /**
     * 客戶催單
     * @param id
     */
    public void remindById(Long id) {
        Orders ordersDB = orderMapper.getById(id);

        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("type", 2);
        map.put("orderId", ordersDB.getId());
        map.put("content", "訂單號" + ordersDB.getNumber());
        map.put("message", "客戶催單");

        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    /**
     * 歷史訂單查詢
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult pageQueryForUser(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<OrderVO> page = orderMapper.pageQuery(ordersPageQueryDTO);

        if (page != null && page.getTotal() > 0) {
            for (OrderVO orderVO : page) {
                //透過orderid獲得訂單明細
                List<OrderDetail> orderDetails = orderDetailMapper.getOrderDetailById(orderVO.getId());
                orderVO.setOrderDetailList(orderDetails);
            }
        }
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 用戶取消訂單
     * @param id
     */
    public void userCancelOrderById(Long id) {
        //根據id獲得訂單
        Orders orders = orderMapper.getById(id);

        if(orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Integer ordersStatus = orders.getStatus();

        //訂單在待支付或待接單的狀態下可以直接取消
        if (ordersStatus > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //訂單在待接單的狀態下需要退款給用戶
        if(ordersStatus == 2) {
            //退款 TODO
        }

        //更新訂單狀態
        Orders ordersDB = new Orders();
        BeanUtils.copyProperties(orders, ordersDB);
        ordersDB.setStatus(Orders.CANCELLED);
        ordersDB.setCancelReason("用戶取消");
        ordersDB.setCancelTime(LocalDateTime.now());
        orderMapper.update(ordersDB);

    }

    /**
     * 再來一單
     * @param id
     */
    public void repetitionById(Long id) {

        //根據id查詢訂單明細
        List<OrderDetail> orderDetailList = orderDetailMapper.getOrderDetailById(id);

        //將訂單明細對象轉為購物車對象
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        //批量插入購物車
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 訂單搜索--訂單明細
     * @param page
     * @return
     */
    private List<OrderVO> getOrderVOlist(Page<OrderVO> page) {
        List<OrderVO> orderVOList = new ArrayList<>();

        List<OrderVO> orderList = page.getResult();
        if(!CollectionUtils.isEmpty(orderList)){
            for (OrderVO orders : orderList) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                String orderDishes = getOrderDishesStr(orders);

                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }

        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     * @param orders
     * @return
     */
    private String getOrderDishesStr(OrderVO orders) {
        //查詢訂單菜品詳情
        List<OrderDetail> orderDishesList = orderDetailMapper.getOrderDetailById(orders.getId());

        //將每一條訂單菜品信息拼接成字符串
        List<String> orderDishList = orderDishesList.stream().map(dish -> {
            String orderDishes = dish.getName() + "*" + dish.getNumber();
            return orderDishes;
        }).collect(Collectors.toList());

        //將該訂單的所有菜品信息拼接在一起
        return String.join(",", orderDishList);
    }

    /**
     * 根据订单id获取地址信息字符串
     * @param ordersSubmitDTO
     * @return
     */
    private String getOrderAddressStr(OrdersSubmitDTO ordersSubmitDTO) {
        //查詢訂單地址詳情
        AddressBook address= addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());

        //將訂單地址中的 省、市、區、詳細地址拼接成字符串
        StringBuilder addressStr = new StringBuilder();
        addressStr.append(address.getProvinceName()).append(address.getCityName()).append(address.getDistrictName()).append(address.getDetail());

        return addressStr.toString();
    }
}
