package com.fantasy.tbs.service.impl;

import com.fantasy.tbs.repository.TimeBookingRepository;
import com.fantasy.tbs.service.InformForgotService;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import liquibase.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * InformForgotServiceImpl.
 *
 * @author ：ljp.
 * @ClassName : InformForgotServiceImpl
 * @Version : 1.0.0
 */

@Service
@Transactional
public class InformForgotServiceImpl implements InformForgotService {

    private final Logger log = LoggerFactory.getLogger(InformForgotServiceImpl.class);
    // TODO: PAGE_SIZE and THREAD_NUM depend on conditions of OS system.
    private static final int PAGE_SIZE = 100;
    private static final int THREAD_NUM = 16;
    private final TimeBookingRepository timeBookingRepository;

    public InformForgotServiceImpl(TimeBookingRepository timeBookingRepository) {
        this.timeBookingRepository = timeBookingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public void informForgotEmployeesForSmallData(String checkTime) {
        if (StringUtil.isEmpty(checkTime)) {
            log.debug("check time is invalid");
            return;
        }
        List<String> forgotEmployees = timeBookingRepository.getForgotEmployees(checkTime);
        for (String personalNumber : forgotEmployees) {
            //TODO: Suggestion: use MQ for sending personalNumber one by one to modules which
            // relate to notification functions. Once the modules get a personalNumber, which should
            // be unique in employee information table , they are able to grab his/her contact
            // information and then make notification. The reason for using MQ is to rise
            // efficiency by asynchronization in case that it might make more consumption by
            // frequently calling interfaces from other modules.
        }

    }


    @Override
    @Transactional(readOnly = true)
    public void informForgotEmployeesForBigData(String checkTime) {
        if (StringUtil.isEmpty(checkTime)) {
            log.debug("check time is invalid");
            return;
        }
        try {
            int totalNum = timeBookingRepository.countForgotEmployees(checkTime);
            int iterateNum = totalNum / PAGE_SIZE + (totalNum % PAGE_SIZE != 0 ? 1 : 0);
            CountDownLatch latch = new CountDownLatch(iterateNum);
            ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(THREAD_NUM);
            long startTime = System.currentTimeMillis();
            RunnableTask.start = -PAGE_SIZE;
            for (int i = 0; i < iterateNum; i++) {
                RunnableTask task = new RunnableTask(PAGE_SIZE, totalNum,
                    latch, checkTime, timeBookingRepository);
                newFixedThreadPool.execute(task);
            }
            latch.await();
            System.out.println(String.format("====Tasks complete, consumption: {%s} ms====", System.currentTimeMillis() - startTime));
            newFixedThreadPool.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class RunnableTask implements Runnable {

        public static Integer start;
        private int pageSize;
        private int totalNum;
        private int startNum;
        private String checkTime;
        private TimeBookingRepository timeBookingRepository;
        private CountDownLatch latch;
        public RunnableTask(int pageSize, int totalNum, CountDownLatch latch,
                            String checkTime, TimeBookingRepository timeBookingRepository) {
            this.latch = latch;
            this.pageSize = pageSize;
            this.totalNum = totalNum;
            this.checkTime = checkTime;
            this.timeBookingRepository = timeBookingRepository;
        }

        @Override
        public void run() {
            try {
                getNextBatch();
            }catch(Exception ex) {
                ex.printStackTrace();
            }finally {
                latch.countDown();
            }
        }

        public void getNextBatch(){
            while (start + this.pageSize < totalNum) {
                synchronized(start) {
                    start += this.pageSize;
                    startNum = start;
                }
                List<String> forgotEmployees = timeBookingRepository.getForgotEmployees(checkTime, startNum, pageSize);
                System.out.println("=====result=====" + forgotEmployees);
                for (String personalNumber : forgotEmployees) {
                    //TODO: same as the suggestion above.
                }
                int result = start + pageSize;
                System.out.println(Thread.currentThread().getName() + "startNum =" + startNum + " pageSize=" + result);
            }
        }
    }
}
