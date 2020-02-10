package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cmc.claimstore.services.WorkingDayIndicator;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.NextWorkingDay;

import java.time.LocalDate;

@Api
@RestController
@RequestMapping(
    path = "/calendar",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class CalendarController {

    private final WorkingDayIndicator workingDayIndicator;

    @Autowired
    public CalendarController(WorkingDayIndicator workingDayIndicator) {
        this.workingDayIndicator = workingDayIndicator;
    }

    @GetMapping(path = "/next-working-day")
    @ApiOperation("Returns next working day from date given")
    public NextWorkingDay getNextWorkingDay(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("date") LocalDate date
    ) {
        try {
            return new NextWorkingDay(workingDayIndicator.getNextWorkingDay(date));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }
}
