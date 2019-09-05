package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Classification;

import java.time.LocalDateTime;
import java.util.HashMap;
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
    private static final Map<String, Object> CASE_DATA = new HashMap<>();
    //private static final ClaimData caseData = SampleClaimData.builder().withExternalId(RAND_UUID).build();
    private static final Classification SECURITY_CLASSIFICATION = Classification.PUBLIC;
    private static final String CALLBACK_RESPONSE_STATUS = "";

    private SampleCaseDetails() {
/*        CASE_DATA.put("id", "1546455160649708");
        CASE_DATA.put("amountType", "RANGE");
        CASE_DATA.put("externalId", "918dea15-cf9d-43a4-b5b8-c40751e5de23");
        CASE_DATA.put("previousServiceCaseReference", "000LR003");
        CASE_DATA.put("externalReferenceNumber", "PBA1234567");
        CASE_DATA.put("submittedOn", "2019-01-02T18:52:39.913603");
        CASE_DATA.put("submitterId", "61");
        CASE_DATA.put("sotSignerName", "sdf sdf sfsf");
        CASE_DATA.put("sotSignerRole", "sdfsdf sd ");
        CASE_DATA.put("preferredCourt", "Bromley County Court and Family Court");
        CASE_DATA.put("submitterEmail", "dharmendrak02@gmail.com");
        CASE_DATA.put("amountLowerValue", 6800);
        CASE_DATA.put("feeAccountNumber", "PBA1234567");
        CASE_DATA.put("amountHigherValue", "1500000");
        CASE_DATA.put("feeAmountInPennies", "75000");
        CASE_DATA.put("housingDisrepairOtherDamages", "MORE_THAN_THOUSAND_POUNDS");
        CASE_DATA.put("personalInjuryGeneralDamages", "MORE_THAN_THOUSAND_POUNDS");
        CASE_DATA.put("housingDisrepairCostOfRepairDamages", "MORE_THAN_THOUSAND_POUNDS");
        CASE_DATA.put("claimSubmissionOperationIndicators", CCDClaimSubmissionOperationIndicators.builder()
                                                            .rpa(CCDYesNoOption.YES)
                                                            .bulkPrint(CCDYesNoOption.YES)
                                                            .sealedClaimUpload(CCDYesNoOption.YES)
                                                            .staffNotification(CCDYesNoOption.YES)
                                                            .claimantNotification(CCDYesNoOption.YES)
                                                            .defendantNotification(CCDYesNoOption.YES)
                                                            .claimIssueReceiptUpload(CCDYesNoOption.YES)
                                                            .build()
        );*/
       //CASE_DATA.put("applicants", caseData.getClaimants());
      // CASE_DATA.put("respondents", caseData.getDefendants());
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
           //.data()
           .securityClassification(SECURITY_CLASSIFICATION)
           .callbackResponseStatus(CALLBACK_RESPONSE_STATUS)
           .build();
    }
}
