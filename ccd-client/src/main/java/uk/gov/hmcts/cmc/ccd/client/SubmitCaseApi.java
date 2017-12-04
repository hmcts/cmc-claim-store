package uk.gov.hmcts.cmc.ccd.client;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.cmc.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.cmc.ccd.client.model.CaseDetails;

@FeignClient(name = "core-case-data-api", url = "${core_case_data_api.url}")
public interface SubmitCaseApi {

    @RequestMapping(method = RequestMethod.POST, value = "{core_case_data_submit_uri}")
    ResponseEntity<CaseDetails> submit(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                       @RequestHeader("ServiceAuthorisation") String serviceAuthorisation,
                                       @PathVariable("user-id") String userId,
                                       @PathVariable("jurisdiction-id") String jurisdictionId,
                                       @PathVariable("case-type-id") String caseTypeId,
                                       @PathVariable("event-id") String eventId,
                                       @RequestParam("ignore-warning") boolean ignoreWarning,
                                       @RequestBody CaseDataContent caseDataContent
    );

}
