package uk.gov.hmcts.cmc.claimstore.controllers;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.CaseEventService;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api
@RestController
@RequestMapping(
    path = "/case-events",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class CaseEventController {

    private final CaseEventService caseEventService;

    @Autowired
    public CaseEventController(CaseEventService caseEventService) {
        this.caseEventService = caseEventService;
    }


    @GetMapping(value = "/find-events-for-case/{ccdCaseId}")
    public List<CaseEvent> findEventsForCase(
        @NotEmpty @NotNull @PathVariable("ccdCaseId") String ccdCaseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {
        return caseEventService.findEventsForCase(authorisation, ccdCaseId);
    }

}
