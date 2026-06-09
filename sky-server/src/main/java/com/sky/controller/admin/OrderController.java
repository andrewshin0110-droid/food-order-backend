package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/order")
@Api(tags = "訂單相關接口")
@Slf4j
public class OrderController {

    @Autowired
        private OrderService orderService;

    /**
     * 取消訂單
     * @param ordersCancelDTO
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("取消訂單")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        log.info("取消訂單: {}",ordersCancelDTO);
        orderService.cancelOrder(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 完成訂單
     * @param id
     * @return
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成訂單")
    public Result complete(@PathVariable("id") Long id){
        log.info("完成訂單: {}", id);
        orderService.completeOrder(id);
        return Result.success();

    }

    /**
     * 接單
     * @param ordersConfirmDTO
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("接單")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("接單: {}",ordersConfirmDTO);
        orderService.confirmOrder(ordersConfirmDTO);
        return Result.success();
    }

    /**
     * 拒單
     * @param ordersRejectionDTO
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("拒單")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("拒單: {}",ordersRejectionDTO);
        orderService.rejectionOrder(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 查詢訂單詳情
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查詢訂單詳情")
    public Result<OrderVO> detail(@PathVariable("id") Long id){
        log.info("查詢訂單詳情: {}", id);
        OrderVO orderVO = orderService.detailOrder(id);
        return Result.success(orderVO);
    }

    /**
     * 派送訂單
     * @param id
     * @return
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送訂單")
    public Result delivery(@PathVariable("id") Long id){
        log.info("派送訂單: {}", id);
        orderService.deliveryOrder(id);
        return Result.success();
    }

    /**
     * 訂單搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("訂單搜索")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO  ordersPageQueryDTO) {
        log.info("訂單搜索: {}",ordersPageQueryDTO);
        PageResult pageResult = orderService.pageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 各個狀態的訂單數量統計
     * @return
     */
    @GetMapping("/statistics")
    @ApiOperation("各個狀態的訂單數量統計")
    public Result<OrderStatisticsVO> statistics() {
        log.info("各個狀態的訂單數量統計");
        OrderStatisticsVO orderStatisticsVO = orderService.statisticsOrder();
        return Result.success(orderStatisticsVO);
    }
}
