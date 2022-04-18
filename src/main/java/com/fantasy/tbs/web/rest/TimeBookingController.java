package com.fantasy.tbs.web.rest;

import com.fantasy.tbs.domain.TimeBookDTO;
import com.fantasy.tbs.service.impl.TimeBookingServiceImpl;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TimeBookingController {

    private final TimeBookingServiceImpl timeBookingService;

    public TimeBookingController(TimeBookingServiceImpl timeBookingService) {
        this.timeBookingService = timeBookingService;
    }

    @PostMapping("/book")
    public ResponseEntity<Void> addTimeBooking(@RequestBody TimeBookDTO timeBookDTO) {
        timeBookingService.bookTime(timeBookDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get-work-time")
    public ResponseEntity<String> getWorkTime(@RequestParam("personalNumber") String personalNumber,
                                              @RequestParam("startTime") ZonedDateTime startTime,
                                              @RequestParam("endTime") ZonedDateTime endTime) {
        List<String> errorList = new ArrayList<>();
        String hours = timeBookingService.getWorkTime(personalNumber, startTime, endTime, errorList);
        return StringUtils.isEmpty(hours) ? ResponseEntity.ok(errorList.get(0)) : ResponseEntity.ok(hours);

        //TODO: For better implementing this feature, we need to consider the situations below:
        // 1. how to deal with work time across a day, like an employee starts to work at 18:00 and ends at
        // 6:00 next day.
        // 2. when employees should make a time book and how many times they should make in one day.

        //TODO: For rising efficiency of this feature, below is my suggestion:
        // 1. create a task scheduler for calculating each employee's working hours by days, by weeks or by months,
        // depending on business requirements (auto calculate on a specific time).
        // 2. save these results in a table and then we do not need to recalculate when asking for someone's working hours.
        // 3. cache these results and expire them maybe in a month for rising read efficiency.
    }
}
