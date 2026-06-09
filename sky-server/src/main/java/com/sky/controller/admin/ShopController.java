package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "店鋪相關接口")
public class ShopController {

    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 設置店鋪的營業狀態
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("設置店鋪的營業狀態")
    public Result setStatus(@PathVariable Integer status) {
        log.info("設置店鋪的營業狀態為:{}", status == 1 ? "營業中" : "打烊中");
        redisTemplate.opsForValue().set(KEY, status);
        return Result.success();
    }

    /**
     * 獲取店鋪的營業狀態
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("獲取店鋪的營業狀態")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("獲取到店鋪的營業狀態為: {}", status == 1 ? "營業中" : "打烊中");
        return Result.success(status);
    }
}
