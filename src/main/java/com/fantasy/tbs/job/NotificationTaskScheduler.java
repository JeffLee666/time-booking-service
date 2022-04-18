package com.fantasy.tbs.job;

import com.fantasy.tbs.service.InformForgotService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * InformScheduleTask.
 *
 * @author ：ljp.
 * @ClassName : InformScheduleTask
 * @Version : 1.0.0
 */
@Component
public class NotificationTaskScheduler {
    //TODO: Assumption: employees should book a time at 18:00:00 and it will
    // start to inform those who forgot at 18:05:00 on the same day.
    private static final String CHECK_TIME = "18:00:00";
    private final InformForgotService informForgotService;

    public NotificationTaskScheduler(InformForgotService informForgotService) {
        this.informForgotService = informForgotService;
    }

    @Scheduled(cron = "0 5 18 * * ?")
    private void configureTasks() {
        //TODO: A company may have small size with less than thousands of employees, we just search once and get all records
        // from database and then do the notification logic. However, consider a company with millions of employees, we ask for
        // all data once, which will lead OOM. Therefore, for the big amount of data, it is better to query data batch by batch
        // and use multi-threads to rise read efficiency. In these two cases, I provide a service with apis for small data and
        // big data.

        // for small data
        // informForgotService.informForgotEmployeesForSmallData(CHECK_TIME);
        // for big data
        informForgotService.informForgotEmployeesForBigData(CHECK_TIME);

        //TODO: For better implementing this feature, we need to consider the situations below:
        // 1. when for notification;
        // 2. what ways to inform, like email, Wechat, phone message, etc;
        // 3. how to deal with employees who resigned and are on leave on that day.
    }
}


