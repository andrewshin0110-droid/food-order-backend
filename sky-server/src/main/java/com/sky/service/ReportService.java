package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {
    /**
     * 營業額統計
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnStatistics(LocalDate begin, LocalDate end);

    /**
     * 用戶數據統計
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    /**
     * 訂單數據統計
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO getOrdersReport(LocalDate begin, LocalDate end);

    /**
     * 銷量排名top10
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO getSalesTop10Repor(LocalDate begin, LocalDate end);

    /**
     * 導出運營數據報表
     * @param response
     */
    void exportBussinessData(HttpServletResponse response);
}
