package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "用戶定單相關接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用戶下單
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用戶下單")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用戶下單: {}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 模擬支付
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("訂單支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("訂單支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易單：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 客戶催單
     * @param id
     * @return
     * @throws Exception
     */
    @GetMapping("/reminder/{id}")
    @ApiOperation("客戶催單")
    public Result reminder(@PathVariable("id") Long id) throws Exception {
        log.info("客戶催單: {}", id);
        orderService.remindById(id);
        return Result.success();
    }

    /**
     * 歷史訂單查詢
     * @param ordersPageQueryDTO
     * @return
     */

    @GetMapping("/historyOrders")
    @ApiOperation("歷史訂單查詢")
    public Result<PageResult> history(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("歷史訂單查詢: {}", ordersPageQueryDTO);
        PageResult page =  orderService.pageQueryForUser(ordersPageQueryDTO);
        return Result.success(page);

    }

    /**
     * 查詢訂單詳情
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查詢訂單詳情")
    public Result<OrderVO> detail(@PathVariable("id") Long id){
        log.info("查詢訂單詳情: {}", id);
        OrderVO orderVO = orderService.detailOrder(id);
        return Result.success(orderVO);
    }

    /**
     * 用戶取消訂單
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("用戶取消訂單")
    public Result cancel(@PathVariable("id") Long id){
        log.info("用戶取消訂單: {}", id);
        orderService.userCancelOrderById(id);
        return Result.success();
    }

    /**
     * 再來一單
     * @param id
     * @return
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再來一單")
    public Result repetition(@PathVariable("id") Long id){
        log.info("再來一單: {}", id);
        orderService.repetitionById(id);
        return Result.success();
    }
}
