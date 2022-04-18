package com.fantasy.tbs;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.fantasy.tbs.repository.TimeBookingRepository;
import com.fantasy.tbs.service.InformForgotService;
import com.fantasy.tbs.service.TimeBookingService;
import com.netflix.discovery.converters.Auto;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ArchTest {

    @Autowired
    private TimeBookingRepository timeBookingRepository;
    @Autowired
    private TimeBookingService timeBookingService;
    @Autowired
    private InformForgotService informForgotService;

    @Test
    void servicesAndRepositoriesShouldNotDependOnWebLayer() {
        JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.fantasy.tbs");

        noClasses()
            .that()
            .resideInAnyPackage("com.fantasy.tbs.service..")
            .or()
            .resideInAnyPackage("com.fantasy.tbs.repository..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..com.fantasy.tbs.web..")
            .because("Services and repositories should not depend on web layer")
            .check(importedClasses);
    }

    @Test
    void testGetWorkTimeFromDb() {
        String number = "static";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        ZonedDateTime startTime = ZonedDateTime.parse("2021-06-09 00:00:00",formatter);
        ZonedDateTime endTime = ZonedDateTime.parse("2021-06-11 00:00:00",formatter);
        String startStr = startTime.format(formatter);
        String endStr = endTime.format(formatter);
        //System.out.println("====result====:" + timeBookingRepository.getWorkTime(number, startStr, endStr));
        System.out.println("====result====:" + timeBookingRepository.getEmployeeAndWorkTimeInfo(number, startStr, endStr));
    }


    @Test
    void testGetWorkTimeFromService() {
        String number = "static1";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        ZonedDateTime startTime = ZonedDateTime.parse("2021-06-09 15:06:32", formatter);
        ZonedDateTime endTime = ZonedDateTime.parse("2021-06-10 15:06:32", formatter);
        List<String> errorMessage = new ArrayList<>();
        System.out.println("====result====:" + timeBookingService.getWorkTime(number, startTime, endTime, errorMessage));

    }


    @Test
    void testGetForgot() {
        String startStr = "2021-06-09 18:00:00";
        //System.out.println("====result===" + informForgotService.informForgotEmployeesForBigData(startStr));
    }

    @Test
    void testGetForgotCountFromDb() {
        String startStr = "2021-06-09 18:00:00";
        //System.out.println("====result====:" + timeBookingRepository.getForgotEmployeesTotal(startStr));
    }

}
