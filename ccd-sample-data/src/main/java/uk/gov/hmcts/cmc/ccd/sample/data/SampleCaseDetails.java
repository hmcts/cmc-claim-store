package uk.gov.hmcts.cmc.ccd.sample.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Classification;

import java.time.LocalDateTime;
import java.util.Map;

import static java.time.LocalDateTime.now;

public class SampleCaseDetails {

    private static final Long CASE_ID = 1513250998636210L;
    private static final String JURISDICTION_ID = "CMC";
    private static final String CASE_TYPE_ID = "MoneyClaimCase";
    private static final LocalDateTime CREATED_DATE = now();
    private static final LocalDateTime LAST_MODIFIED = now();
    private static final String CASE_STATE = "open";
    private static final Integer LOCKED_BY_USER_ID = 1;
    private static final Integer SECURITY_LEVEL = 1;
    private static final Classification SECURITY_CLASSIFICATION = Classification.PUBLIC;
    private static final String CALLBACK_RESPONSE_STATUS = "";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SampleCaseDetails() {
    }

    private CaseDetails.CaseDetailsBuilder caseDetailsBuilder = CaseDetails.builder()
        .id(CASE_ID)
        .jurisdiction(JURISDICTION_ID)
        .caseTypeId(CASE_TYPE_ID)
        .createdDate(CREATED_DATE)
        .lastModified(LAST_MODIFIED)
        .state(CASE_STATE)
        .lockedBy(LOCKED_BY_USER_ID)
        .securityLevel(SECURITY_LEVEL)
        .securityClassification(SECURITY_CLASSIFICATION)
        .callbackResponseStatus(CALLBACK_RESPONSE_STATUS);

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
            .data(convertCCDCaseToMap(SampleCCDCaseData.getCCDLegalCase()))
            .securityClassification(SECURITY_CLASSIFICATION)
            .callbackResponseStatus(CALLBACK_RESPONSE_STATUS)
            .build();
    }

    public CaseDetails buildRepresentativeCaseDetails() {
        return caseDetailsBuilder.data(convertCCDCaseToMap(SampleCCDCaseData.getCCDLegalCase())).build();
    }

    public CaseDetails buildCitizenCaseDetails() {
        return caseDetailsBuilder.data(convertCCDCaseToMap(
               SampleCCDCaseData.getCCDCitizenCaseWithDefault())).build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertCCDCaseToMap(CCDCase ccdCase) {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return (Map<String, Object>) objectMapper.convertValue(ccdCase, Map.class);
    }

}
