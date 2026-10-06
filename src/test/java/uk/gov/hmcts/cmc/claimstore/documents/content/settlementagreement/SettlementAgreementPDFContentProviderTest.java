package uk.gov.hmcts.cmc.claimstore.documents.content.settlementagreement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cmc.claimstore.documents.content.PartyDetailsContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SettlementAgreementPDFContentProviderTest {

    private final Claim claimWithDefaultResponse = SampleClaim.getWithDefaultResponse();

    private Claim claimWithSettlement;

    private Claim claimWithPaymentIntention;

    private static final String SETTLEMENT_FORM_NAME_OFFERS_ROUTE = "OCON Settlement Agreement";
    private static final String SETTLEMENT_FORM_NAME_ADMISSIONS_ROUTE = "OCON Settlement Agreement A";

    private final SettlementAgreementPDFContentProvider classToTest =
        new SettlementAgreementPDFContentProvider(new PartyDetailsContentProvider());

    @BeforeEach
    public void setUp() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.builder().build(), MadeBy.DEFENDANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);

        Settlement settlementWithInstalments = new Settlement();
        settlementWithInstalments.makeOffer(SampleOffer.builderWithPaymentIntention().build(), MadeBy.DEFENDANT, null);
        settlementWithInstalments.accept(MadeBy.CLAIMANT, null);

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

    @Test
    public void shouldThrowNullPointerWhenNullClaim() {
        assertThrows(NullPointerException.class, () -> {
            classToTest.createContent(null);
        });
    }

    @Test
    public void shouldThrowIllegalArgumentWhenNoResponse() {
        assertThrows(IllegalArgumentException.class, () -> {
            classToTest.createContent(claimWithDefaultResponse);
        });
    }

    @Test
    public void shouldReturnContentMapWithAllCommonFields() {
        Map<String, Object> contentMap = classToTest.createContent(claimWithSettlement);
        assertCommonFieldsIsPresent(contentMap);
    }

    @Test
    public void shouldReturnOconSettlementAFormNameInContentMapWhenPaymentIntention() {
        Map<String, Object> contentMap = classToTest.createContent(claimWithSettlement);
        assertCommonFieldsIsPresent(contentMap);
        assertTrue(contentMap.containsValue(SETTLEMENT_FORM_NAME_OFFERS_ROUTE));
    }

    @Test
    public void shouldReturnOconSettlementFormNameInContentMapWhenAdmission() {
        Map<String, Object> contentMap = classToTest.createContent(claimWithPaymentIntention);
        assertCommonFieldsIsPresent(contentMap);
        assertTrue(contentMap.containsValue(SETTLEMENT_FORM_NAME_ADMISSIONS_ROUTE));
    }

    private void assertCommonFieldsIsPresent(Map<String, Object> contentMap) {
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
