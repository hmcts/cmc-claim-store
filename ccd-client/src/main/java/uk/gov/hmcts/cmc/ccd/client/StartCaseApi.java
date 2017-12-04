package uk.gov.hmcts.cmc.ccd.client;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.cmc.ccd.client.model.StartEventResponse;

@FeignClient(name = "core-case-data-api", url = "${core_case_data_api.url}")
public interface StartCaseApi {

    @RequestMapping(method = RequestMethod.GET, value = "{core_case_data_start_uri}")
    ResponseEntity<StartEventResponse> start(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                             @RequestHeader("ServiceAuthorisation") String serviceAuthorisation,
                                             @PathVariable("user-id") String userId,
                                             @PathVariable("jurisdiction-id") String jurisdictionId,
                                             @PathVariable("case-type-id") String caseTypeId,
                                             @PathVariable("event-id") String eventId,
                                             @RequestParam("ignore-warning") boolean ignoreWarning
    );


}
