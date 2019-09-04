package uk.gov.hmcts.cmc.domain.models.sampledata;


import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Classification;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDateTime.now;

public class SampleCaseDetails {

    private static final Long CASE_ID = 3L;
    private static final String JURISDICTION_ID = "CMC";
    private static final String CASE_TYPE_ID = "MoneyClaimCase";
    private static final LocalDateTime CREATED_DATE = now();
    private static final LocalDateTime LAST_MODIFIED = now();
    private static final String CASE_STATE = "";
    private static final Integer LOCKED_BY_USER_ID = 1;
    private static final Integer SECURITY_LEVEL = 1;
    private static final Map<String, Object> CASE_DATA = new HashMap<>();
    private static final Classification SECURITY_CLASSIFICATION = Classification.PUBLIC;
    private static final String CALLBACK_RESPONSE_STATUS = "";

    private SampleCaseDetails() {
    }

    public static SampleCaseDetails builder() {
        return new SampleCaseDetails();
    }

   public CaseDetails build() {
       return CaseDetails.builder()
           .id(CASE_ID)
           .jurisdiction(JURISDICTION_ID)
           .caseTypeId(CASE_TYPE_ID)
           .createdDate(CREATED_DATE)
           .lastModified(LAST_MODIFIED)
           .state(CASE_STATE)
           .lockedBy(LOCKED_BY_USER_ID)
           .securityLevel(SECURITY_LEVEL)
           .data(CASE_DATA)
           .securityClassification(SECURITY_CLASSIFICATION)
           .callbackResponseStatus(CALLBACK_RESPONSE_STATUS)
           .build();
    }
}
