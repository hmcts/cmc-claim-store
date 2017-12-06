package uk.gov.hmcts.cmc.ccd.client;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.cmc.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.cmc.ccd.client.model.CaseDetails;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "core-case-data-api", url = "${core_case_data.api.url}")
public interface SubmitCaseApi {

    String SUBMIT_CASE_URI = "/citizens/{userId}/jurisdictions/{jurisdictionId}/case-types/{caseTypeId}"
        + "/cases/{caseId}/events";

    @RequestMapping(method = RequestMethod.POST, value = SUBMIT_CASE_URI)
    ResponseEntity<CaseDetails> submit(@RequestHeader(AUTHORIZATION) String authorisation,
                                       @RequestHeader("ServiceAuthorisation") String serviceAuthorisation,
                                       @PathVariable String userId,
                                       @PathVariable String jurisdictionId,
                                       @PathVariable String caseTypeId,
                                       @PathVariable String caseId,
                                       @RequestParam("ignore-warning") boolean ignoreWarning,
                                       @RequestBody CaseDataContent caseDataContent
    );

}
