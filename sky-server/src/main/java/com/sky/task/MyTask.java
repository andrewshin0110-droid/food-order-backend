package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 自訂義定時任務類
 */
@Component
@Slf4j
public class MyTask {

    /**
     * 定時任務 每5秒觸發一次
     */
    //@Scheduled   (cron = "0/5 * * * * ?")
    public void executeTask(){
        log.info("定時執行任務: {}", new Date());
    }
}
