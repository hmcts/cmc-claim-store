package uk.gov.hmcts.cmc.claimstore.documents.content.settlementagreement;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.documents.content.PartyDetailsContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.Assert.*;

public class SettlementAgreementPDFContentProviderTest {

    private Claim claimWithDefaultResponse = SampleClaim.getWithDefaultResponse();

    private Claim claimWithSettlement;

    private Claim claimWithPaymentIntention;

    private final SettlementAgreementPDFContentProvider classToTest =
        new SettlementAgreementPDFContentProvider(new PartyDetailsContentProvider());

    @Before
    public void setup() {
        Settlement settlement= new Settlement();
        settlement.makeOffer(SampleOffer.builder().build(), MadeBy.DEFENDANT);
        settlement.accept(MadeBy.CLAIMANT);

        Settlement settlementWithInstalments = new Settlement();
        settlementWithInstalments.makeOffer(SampleOffer.builderWithPaymentIntention().build(), MadeBy.DEFENDANT);
        settlementWithInstalments.accept(MadeBy.CLAIMANT);

        claimWithSettlement = SampleClaim
            .builder()
            .withSettlementReachedAt(LocalDateTime.now())
            .withResponse(SampleResponse.validDefaults())
            .withSettlement(settlement)
            .build();

        claimWithPaymentIntention = SampleClaim
            .builder()
            .withSettlementReachedAt(LocalDateTime.now())
            .withResponse(SampleResponse.validDefaults())
            .withSettlement(settlementWithInstalments)
            .build();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenNullClaim(){
        classToTest.createContent(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentWhenNoResponse(){
        classToTest.createContent(claimWithDefaultResponse);
    }

    @Test
    public void shouldReturnContentMapWhenClaimOfferWithNoPaymentIntention(){
        Map<String, Object> contentMap = classToTest.createContent(claimWithSettlement);
        assertCommonFieldsIsPresent(contentMap);
        assertFalse(contentMap.containsKey("firstPaymentDate"));
    }

    @Test
    public void shouldReturnContentMapWhenClaimOfferWithPaymentIntentionIsPassed(){
        Map<String, Object> contentMap = classToTest.createContent(claimWithPaymentIntention);
        assertCommonFieldsIsPresent(contentMap);
        assertTrue(contentMap.containsKey("firstPaymentDate"));
    }

    private void assertCommonFieldsIsPresent(Map<String, Object> contentMap){
        assertTrue(contentMap.containsKey("settlementReachedAt"));
        assertTrue(contentMap.containsKey("acceptedOffer"));
        assertTrue(contentMap.containsKey("settlementReachedAt"));
        assertTrue(contentMap.containsKey("acceptedOfferCompletionDate"));
        assertTrue(contentMap.containsKey("claim"));
        assertTrue(contentMap.containsKey("claimant"));
        assertTrue(contentMap.containsKey("defendant"));
        assertTrue(contentMap.containsKey("formName"));
    }
}
