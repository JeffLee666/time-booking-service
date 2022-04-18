package com.fantasy.tbs.service;

import java.util.List;

/**
 * InformForgotService.
 *
 * @author ：ljp.
 * @ClassName : InformForgotService
 * @modifier ：
 * @date ：4/17/2022 5:01 PM
 * @Version : 1.0.0
 */
public interface InformForgotService {

    /**
     *  design for big amount of data to get all employees who forgot to book
     *  on a specific time.
     * @param checkTime
     * @return List<String>
     */
    void informForgotEmployeesForBigData(String checkTime);

    /**
     *  design for big amount of data to get all employees who forgot to book
     *  on a specific time.
     * @param checkTime
     * @return List<String>
     */
    void informForgotEmployeesForSmallData(String checkTime);
}
