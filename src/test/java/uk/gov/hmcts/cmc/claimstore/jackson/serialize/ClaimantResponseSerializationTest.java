package uk.gov.hmcts.cmc.claimstore.jackson.serialize;

import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponseAcceptation;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.Assert.assertThat;


public class ClaimantResponseSerializationTest {

    private JsonMapper processor = new JsonMapper(new CCDAdapterConfig().ccdObjectMapper());
    private static final String JASON_PATH_PREFIX = "$.";

    @Test
    public void claimantResponseForDefendantPartAdmitWithImmediatePayment() {
        ResponseAcceptation responseAcceptation = SampleResponseAcceptation.partAdmitPayImmediately();
        String json = processor.toJson(responseAcceptation);
        assertCommon(json);
        assertThat(json, hasNoJsonPath(JASON_PATH_PREFIX+"CourtDetermination"));
    }

    @Test
    public void claimantResponseCounterOfferForDefendantPartAdmitPayBySetDate() {
        ResponseAcceptation responseAcceptation = SampleResponseAcceptation.partAdmitPayBySetDate();
        String json = processor.toJson(responseAcceptation);
        assertCommon(json);
        assertThat(json, hasJsonPath(JASON_PATH_PREFIX+"courtDetermination"));
        assertThat(json, hasJsonPath(JASON_PATH_PREFIX+"claimantPaymentIntention.paymentOption", equalToIgnoringCase("BY_SPECIFIED_DATE")));
    }

    @Test
    public void claimantResponseCounterOfferForDefendantPartAdmitPayByInstalments() {
        ResponseAcceptation responseAcceptation = SampleResponseAcceptation.partAdmitPayByInstalments();
        String json = processor.toJson(responseAcceptation);
        assertCommon(json);
        assertThat(json, hasJsonPath(JASON_PATH_PREFIX+"courtDetermination.courtDecision"));
        assertThat(json, hasJsonPath(JASON_PATH_PREFIX+"courtDetermination.courtPaymentIntention"));
        assertThat(json, hasJsonPath(JASON_PATH_PREFIX+"claimantPaymentIntention.paymentOption", equalToIgnoringCase("INSTALMENTS")));
    }

    @Test
    public void claimantResponseRejectDefendantOffer() {
        ResponseRejection responseRejection = (ResponseRejection) SampleClaimantResponse.ClaimantResponseRejection.validDefaultRejection();
        String json = processor.toJson(responseRejection);
        assertThat(json, isJson());
        assertThat(json, hasJsonPath(JASON_PATH_PREFIX+"claimantResponseType",equalToIgnoringCase("REJECTION")));
        assertThat(json, hasJsonPath(JASON_PATH_PREFIX+"freeMediation",equalTo(false)));
        assertThat(json, hasJsonPath(JASON_PATH_PREFIX+"reason",equalToIgnoringCase("Some valid reason")));
    }

    private void assertCommon(String json){
        assertThat(json, isJson());
        assertThat(json, hasJsonPath(JASON_PATH_PREFIX+"claimantResponseType"));
        assertThat(json, hasJsonPath(JASON_PATH_PREFIX+"amountPaid"));
        assertThat(json, hasJsonPath(JASON_PATH_PREFIX+"formaliseOption"));
    }

}
