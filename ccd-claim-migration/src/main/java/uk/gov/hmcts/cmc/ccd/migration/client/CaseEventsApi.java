package uk.gov.hmcts.cmc.ccd.migration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "core-case-data-api",
    url = "${core_case_data.api.url}",
    configuration = CoreCaseDataConfiguration.class
)
public interface CaseEventsApi {

    @RequestMapping(
        method = RequestMethod.GET,
        value = "/caseworkers/{userId}/jurisdictions/{jurisdictionId}/case-types/{caseType}/cases/{caseId}/events",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    List<CaseEventDetails> findEventDetailsForCase(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable("userId") String userId,
        @PathVariable("jurisdictionId") String jurisdictionId,
        @PathVariable("caseType") String caseType,
        @PathVariable("caseId") String caseId
    );
}
