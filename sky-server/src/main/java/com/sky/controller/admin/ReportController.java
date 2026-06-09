package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/report")
@Api(tags = "統計數據相關接口")
@Slf4j
public class ReportController {


    @Autowired
    private ReportService reportService;

    /**
     * 營業額統計
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("營業額統計")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")  LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")  LocalDate end) {
        log.info("營業額統計: {},{}", begin, end);
        return Result.success(reportService.getTurnStatistics(begin, end));
    }

    /**
     * 用戶數據統計
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用戶數據統計")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")  LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")  LocalDate end) {
        log.info("用戶數據統計: {},{}", begin, end);
        return Result.success(reportService.getUserStatistics(begin, end));
    }

    /**
     * 訂單數據統計
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/ordersStatistics")
    @ApiOperation("訂單數據統計")
    public Result<OrderReportVO> ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")  LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")  LocalDate end) {
        log.info("訂單數據統計: {},{}", begin, end);
        return Result.success(reportService.getOrdersReport(begin, end));
    }

    /**
     * 訂單數據統計
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/top10")
    @ApiOperation("銷量排名top10")
    public Result<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd")  LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")  LocalDate end) {
        log.info("銷量排名top10: {},{}", begin, end);
        return Result.success(reportService.getSalesTop10Repor(begin, end));
    }

    @GetMapping("/export")
    @ApiOperation("導出運營數據報表")
    public void exportReport(HttpServletResponse response) {
        log.info("導出運營數據報表: {}" , response);
        reportService.exportBussinessData(response);
    }

}
