package com.fantasy.tbs.service.impl;

import com.fantasy.tbs.domain.TimeBookDTO;
import com.fantasy.tbs.domain.TimeBooking;
import com.fantasy.tbs.repository.TimeBookingRepository;
import com.fantasy.tbs.service.TimeBookingService;
import com.fantasy.tbs.service.mapper.TimeBookMapper;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import liquibase.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * Service Implementation for managing {@link TimeBooking}.
 */
@Service
@Transactional
public class TimeBookingServiceImpl implements TimeBookingService {

    private final Logger log = LoggerFactory.getLogger(TimeBookingServiceImpl.class);

    private final TimeBookingRepository timeBookingRepository;
    private final TimeBookMapper timeBookMapper;

    public TimeBookingServiceImpl(TimeBookingRepository timeBookingRepository, TimeBookMapper timeBookMapper) {
        this.timeBookingRepository = timeBookingRepository;
        this.timeBookMapper = timeBookMapper;
    }

    @Override
    public TimeBooking save(TimeBooking timeBooking) {
        log.debug("Request to save TimeBooking : {}", timeBooking);
        return timeBookingRepository.save(timeBooking);
    }

    @Override
    public Optional<TimeBooking> partialUpdate(TimeBooking timeBooking) {
        log.debug("Request to partially update TimeBooking : {}", timeBooking);

        return timeBookingRepository
            .findById(timeBooking.getId())
            .map(
                existingTimeBooking -> {
                    if (timeBooking.getBooking() != null) {
                        existingTimeBooking.setBooking(timeBooking.getBooking());
                    }
                    if (timeBooking.getPersonalNumber() != null) {
                        existingTimeBooking.setPersonalNumber(timeBooking.getPersonalNumber());
                    }

                    return existingTimeBooking;
                }
            )
            .map(timeBookingRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeBooking> findAll() {
        log.debug("Request to get all TimeBookings");
        return timeBookingRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TimeBooking> findOne(Long id) {
        log.debug("Request to get TimeBooking : {}", id);
        return timeBookingRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete TimeBooking : {}", id);
        timeBookingRepository.deleteById(id);
    }

    @Override
    public void bookTime(TimeBookDTO timeBookDTO) {
        timeBookingRepository.save(timeBookMapper.toTimeBooking(timeBookDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public String getWorkTime(String personalNumber, ZonedDateTime startTime,
                              ZonedDateTime endTime,  List<String> errorList) {

        //TODO: Assumption: an employ should book time at least twice in a day. The earliest time
        // is considered as start time while the latest time is end time. Also, if an employee books time
        // less than twice, it would consider ZERO working hour on that day.
        if (!checkIfParamsValid(personalNumber, startTime, endTime, errorList)) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startTimeStr = getMidNightTime(startTime).format(formatter);
        String endTimeStr = getMidNightTime(endTime.plusDays(1)).format(formatter);
        //TODO: For better efficiency, a composite index included booking and personalNumber is suggested.
        List<Double> workTimeList = timeBookingRepository.getWorkTime(personalNumber, startTimeStr, endTimeStr);
        if (CollectionUtils.isEmpty(workTimeList)) {
            String errorMessage = " there is no record for " + personalNumber;
            errorList.add(errorMessage);
            return null;
        }
        double totalWorkTimeInSec =  workTimeList.stream().mapToDouble(Double::doubleValue).sum();
        double hours = totalWorkTimeInSec / 3600d;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(hours);
    }

    private boolean checkIfParamsValid(String personalNumber, ZonedDateTime startTime,
                                       ZonedDateTime endTime, List<String> errorList) {
        if (StringUtil.isEmpty(personalNumber)
            || Objects.isNull(startTime)
            || Objects.isNull(endTime)) {
            String errorMessage = "some parameters are invalid.";
            errorList.add(errorMessage);
            return false;
        }
        return true;
    }

    private ZonedDateTime getMidNightTime(ZonedDateTime dateTime) {
        return ZonedDateTime.of(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(),
            0, 0, 0, 0, dateTime.getZone());
    }
}
