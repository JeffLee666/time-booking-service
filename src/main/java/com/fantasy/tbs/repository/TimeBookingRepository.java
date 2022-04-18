package com.fantasy.tbs.repository;

import com.fantasy.tbs.domain.TimeBooking;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the TimeBooking entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TimeBookingRepository extends JpaRepository<TimeBooking, Long> {

    /**
     * Feature1: get everyday work hours for an employee.
     * @param personalNumber
     * @param startTime
     * @param endTime
     * @return List<Double>
     */
    @Query(value = "select time_to_sec(timediff(maxt, mint)) " +
        "from (select t1.personal_number, max(t1.booking) as maxt, min(t1.booking) as mint " +
        "from time_booking t1 where t1.booking between :startTime and :endTime " +
        "and t1.personal_number = :personalNumber " +
        "group by substring(t1.booking, 1, 10)) temp", nativeQuery = true)
    List<Double> getWorkTime(@Param("personalNumber") String personalNumber,
                           @Param("startTime") String startTime,
                           @Param("endTime") String endTime);

    /**
     * Feature1: get an employee's work time info.
     * @param personalNumber
     * @param startTime
     * @param endTime
     * @return List<Double>
     */
    @Query(value = "select tb.booking " +
        "from time_booking tb " +
        "where tb.booking " +
        "between :startTime and :endTime " +
        "and tb.personal_number = :personalNumber " +
        "order by tb.booking", nativeQuery = true)
    List<String> getEmployeeAndWorkTimeInfo(@Param("personalNumber") String personalNumber,
                             @Param("startTime") String startTime,
                             @Param("endTime") String endTime);

    /**
     * Feature2: design for small amount of data to get all employees who forgot to book
     * on a specific time.
     * @param checkTime
     * @return List<String>
     */
    @Query(value = "select all_number.personal_number from (" +
        "select personal_number from time_booking " +
        "group by personal_number ) all_number " +
        "left join (" +
        "select personal_number from time_booking where booking >= :checkTime " +
        "group by personal_number) today_number " +
        "on all_number.personal_number = today_number.personal_number " +
        "where today_number.personal_number is null ", nativeQuery = true)
    List<String> getForgotEmployees(@Param("checkTime") String checkTime);

    /**
     * Feature2: design for big amount of data to get the total of all employees who forgot
     * to book on a specific time.
     * @param checkTime
     * @return int
     */
    @Query(value = "select count(all_number.personal_number) from (" +
        "select personal_number from time_booking " +
        "group by personal_number ) all_number " +
        "left join (" +
        "select personal_number from time_booking where booking >= :checkTime " +
        "group by personal_number) today_number " +
        "on all_number.personal_number = today_number.personal_number " +
        "where today_number.personal_number is null ", nativeQuery = true)
    int countForgotEmployees(@Param("checkTime") String checkTime);

    /**
     * Feature2: design for big amount of data to get all employees who forgot to book
     * on a specific time.
     * @param checkTime
     * @return List<String>
     */
    @Query(value = "select all_number.personal_number from (" +
        "select personal_number from time_booking " +
        "group by personal_number ) all_number " +
        "left join (" +
        "select personal_number from time_booking where booking >= :checkTime " +
        "group by personal_number) today_number " +
        "on all_number.personal_number = today_number.personal_number " +
        "where today_number.personal_number is null " +
        "order by all_number.personal_number " +
        "limit :startNum, :pageSize", nativeQuery = true)
    List<String> getForgotEmployees(@Param("checkTime") String checkTime,
                                    @Param("startNum") int startNum,
                                    @Param("pageSize") int pageSize);


}
