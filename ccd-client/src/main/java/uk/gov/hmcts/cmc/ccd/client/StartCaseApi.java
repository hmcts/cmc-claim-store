package uk.gov.hmcts.cmc.ccd.client;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.cmc.ccd.client.model.StartEventResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "core-case-data-api", url = "${core_case_data.api.url}")
public interface StartCaseApi {

    String START_CASE_URI = "/citizens/{userId}/jurisdictions/{jurisdictionId}/case-types/{caseTypeId}/"
        + "event-triggers/{eventId}/token";

    @RequestMapping(method = RequestMethod.GET, value = START_CASE_URI)
    ResponseEntity<StartEventResponse> start(@RequestHeader(AUTHORIZATION) String authorisation,
                                             @RequestHeader("ServiceAuthorisation") String serviceAuthorisation,
                                             @PathVariable String userId,
                                             @PathVariable String jurisdictionId,
                                             @PathVariable String caseTypeId,
                                             @PathVariable String eventId,
                                             @RequestParam("ignore-warning") boolean ignoreWarning
    );


}
