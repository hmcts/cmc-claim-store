package uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata;

import uk.gov.hmcts.cmc.claimstore.models.DefendantResponse;
import uk.gov.hmcts.cmc.claimstore.models.ResponseData;
import uk.gov.hmcts.cmc.claimstore.models.party.Party;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleParty;

import java.time.LocalDateTime;

public final class SampleDefendantResponse {

    public static final String DEFENDANT_EMAIL = "j.smith@example.com";
    public static final Long DEFENDANT_ID = 1L;
    public static final Long CLAIM_ID = 2L;
    public static final Long RESPONSE_ID = 2L;

    private SampleDefendantResponse() {
        // NO-OP
    }

    public static DefendantResponse getDefault() {
        return new DefendantResponse(
            RESPONSE_ID,
            CLAIM_ID,
            DEFENDANT_ID,
            DEFENDANT_EMAIL,
            SampleResponseData.validDefaults(),
            LocalDateTime.now()
        );
    }

    public static DefendantResponse getWithoutMobileNumber() {
        return new DefendantResponse(
            RESPONSE_ID,
            CLAIM_ID,
            DEFENDANT_ID,
            DEFENDANT_EMAIL,
            SampleResponseData.builder()
                .withDefendantDetails(
                    SampleParty.builder().withMobilePhone("").individual()
                ).build(),
            LocalDateTime.now()
        );
    }

    public static DefendantResponse getWithoutFreeMediation() {
        return new DefendantResponse(
            RESPONSE_ID,
            CLAIM_ID,
            DEFENDANT_ID,
            DEFENDANT_EMAIL,
            SampleResponseData.builder()
                .withMediation(ResponseData.FreeMediationOption.NO)
                .build(),
            LocalDateTime.now()
        );
    }

    public static DefendantResponse getWithoutFreeMediationFullDefence() {
        return new DefendantResponse(
            RESPONSE_ID,
            CLAIM_ID,
            DEFENDANT_ID,
            DEFENDANT_EMAIL,
            SampleResponseData.builder()
                .withMediation(ResponseData.FreeMediationOption.NO)
                .withResponseType(ResponseData.ResponseType.OWE_NONE)
                .build(),
            LocalDateTime.now()
        );
    }

    public static DefendantResponse getWithFullDefence() {
        return new DefendantResponse(
            RESPONSE_ID,
            CLAIM_ID,
            DEFENDANT_ID,
            DEFENDANT_EMAIL,
            SampleResponseData.builder()
                .withResponseType(ResponseData.ResponseType.OWE_NONE)
                .build(),
            LocalDateTime.now()
        );
    }

    public static DefendantResponse getWithDefendantDetails(Party defendantDetails) {
        return new DefendantResponse(
            RESPONSE_ID,
            CLAIM_ID,
            DEFENDANT_ID,
            DEFENDANT_EMAIL,
            SampleResponseData.builder()
                .withResponseType(ResponseData.ResponseType.OWE_NONE)
                .withDefendantDetails(defendantDetails)
                .build(),
            LocalDateTime.now()
        );
    }

}
