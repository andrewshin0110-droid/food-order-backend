package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;



    /**
     * 營業額統計
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnStatistics(LocalDate begin, LocalDate end) {
        //當前集合用於存放從begin到end範圍內的每天的日期
        List<LocalDate> dateList = new ArrayList();
        dateList.add(begin);

        while(!begin.equals(end)){
            //日期計算 計算指定日期的後一天對應日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }


        //存放每天的營業額
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //查詢date日期對應的營業額數據(營業額: 狀態為"已完成"的訂單金額合計)
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //select sum(amount) from orders where order_time > beginTime and order_time < endTime and status = 5
            Map map =  new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover =  orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);

        }

        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 用戶數據統計
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {

        //當前集合用於存放從begin到end範圍內的每天的日期
        List<LocalDate> dateList = new ArrayList();
        dateList.add(begin);

        while(!begin.equals(end)){
            //日期計算 計算指定日期的後一天對應日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天的新增用戶數量 select count(id) from user where create_time < ? and create > ?
        List<Integer> newUserList = new ArrayList<>();
        //存放每天的宗用戶數量 select count(id) from user where create_time < ?
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            //查詢date日期對應的營業額數據(營業額: 狀態為"已完成"的訂單金額合計)
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("endTime", endTime);

            //總用戶數量
            Integer totalUser = userMapper.countByMap(map);

            map.put("beginTime", beginTime);
            //新增用戶數量
            Integer newUser = userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);

        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    /**
     * 訂單數據統計
     *
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrdersReport(LocalDate begin, LocalDate end) {

        //當前集合用於存放從begin到end範圍內的每天的日期
        List<LocalDate> dateList = new ArrayList();
        dateList.add(begin);

        while(!begin.equals(end)){
            //日期計算 計算指定日期的後一天對應日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天的訂單總數
        List<Integer> totalOrderList = new ArrayList();
        //存放每天的有效訂單數
        List<Integer> validOrderList = new ArrayList();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //查詢每天的訂單總數
            Integer orderCount = getOrderCount(beginTime, endTime, null);
            //查詢每天的有效訂單數
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            totalOrderList.add(orderCount);
            validOrderList.add(validOrderCount);
        }

        //計算時間區間內的訂單總量
        Integer totalOrderCount = totalOrderList.stream().reduce(Integer::sum).get();
        //計算時間區間內的有效訂單總量
        Integer validOrderCount = validOrderList.stream().reduce(Integer::sum).get();
        //計算訂單完成率
        Double orderCompletionRate = 0.0;
        if(totalOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(totalOrderList, ","))
                .validOrderCountList(StringUtils.join(validOrderList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 銷量排名top10
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getSalesTop10Repor(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salseTop10 = orderMapper.getSalseTop10(beginTime, endTime);

        List<String> names = salseTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");

        List<Integer> numbers = salseTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        return SalesTop10ReportVO
                .builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 導出運營數據報表
     * @param response
     */
    public void exportBussinessData(HttpServletResponse response) {
        //查詢數據庫 獲取營業數據-- 查詢最近30天數據
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        //查詢概覽數據
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //通過POI將數據寫入EXCEL文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/運營數據報表模板.xlsx");

        try {
            //基於模板文件創建一個Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //獲取表格文件sheet頁
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充數據
            sheet.getRow(1).getCell(1).setCellValue("時間" + dateBegin + "至" +  dateEnd);

            //獲取第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //獲取第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明細數據
            for (int i = 0; i < 30; i++){
                LocalDate date = dateBegin.plusDays(i);
                //查詢某一天的營業額數據
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //獲得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //通過輸出流將EXCEL文件下載到客戶端瀏覽器
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);

            //關閉數據
            outputStream.close();
            excel.close();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 根據條件統計訂單數量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("beginTime", begin);
        map.put("endTime", end);
        map.put("status", status);

        return orderMapper.countByMap(map);


    }
}
