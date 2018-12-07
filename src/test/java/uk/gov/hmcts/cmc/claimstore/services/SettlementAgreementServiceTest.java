package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SettlementAgreementServiceTest {

    private static final String AUTHORISATION = "Bearer aaa";

    private SettlementAgreementService settlementAgreementService;

    @Mock
    private ClaimService claimService;

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private AppInsights appInsights;

    @Before
    public void setup() {
        settlementAgreementService =
            new SettlementAgreementService(claimService, caseRepository, eventProducer, appInsights);
    }

    @Test
    public void shouldSuccessfullyRejectSettlementAgreement() {
        Claim claimWithSettlementAgreement = buildClaimWithSettlementAgreementOffer();

        when(claimService.getClaimByExternalId(claimWithSettlementAgreement.getExternalId(), AUTHORISATION))
            .thenReturn(claimWithSettlementAgreement);

        settlementAgreementService.reject(claimWithSettlementAgreement, AUTHORISATION);

        verify(caseRepository).updateSettlement(eq(claimWithSettlementAgreement), any(Settlement.class),
            eq(AUTHORISATION), eq("SETTLEMENT_AGREEMENT_REJECTED_BY_DEFENDANT"));
    }

    @Test(expected = ConflictException.class)
    public void shouldRaiseConflictExceptionOnSettlementAgreementAlreadyRejected() {
        Claim claimWithSettlementAgreementRejected = buildClaimWithSettlementAgreementRejected();
        settlementAgreementService.reject(claimWithSettlementAgreementRejected, AUTHORISATION);
    }

    @Test(expected = ConflictException.class)
    public void shouldRaiseConflictExceptionOnClaimWithNoSettlement() {
        Claim claim = SampleClaim.builder().build();
        settlementAgreementService.reject(claim, AUTHORISATION);
    }

    @Test(expected = ConflictException.class)
    public void shouldRaiseConflictExceptionOnRejectWhenSettlementAgreementAlreadyReached() {
        Claim claim = buildClaimWithSettlementReached();
        settlementAgreementService.reject(claim, AUTHORISATION);
    }

    @Test
    public void shouldSuccessfullyCountersignSettlementAgreement() {
        Claim claimWithSettlementAgreement = buildClaimWithSettlementAgreementOffer();

        when(claimService.getClaimByExternalId(claimWithSettlementAgreement.getExternalId(), AUTHORISATION))
            .thenReturn(claimWithSettlementAgreement);

        settlementAgreementService.countersign(claimWithSettlementAgreement, AUTHORISATION);

        verify(caseRepository).updateSettlement(eq(claimWithSettlementAgreement), any(Settlement.class),
            eq(AUTHORISATION), eq("SETTLEMENT_AGREEMENT_COUNTERSIGNED_BY_DEFENDANT"));
    }

    @Test(expected = ConflictException.class)
    public void shouldRaiseConflictExceptionWhenCountersigningAgreementAlreadyRejected() {
        Claim claim = buildClaimWithSettlementAgreementRejected();
        settlementAgreementService.countersign(claim, AUTHORISATION);
    }

    private Claim buildClaimWithSettlementAgreementOffer() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.builder().build(), MadeBy.CLAIMANT);
        settlement.acceptCourtDetermination(MadeBy.CLAIMANT);

        return SampleClaim.builder().withSettlement(settlement).build();
    }

    private Claim buildClaimWithSettlementAgreementRejected() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.builder().build(), MadeBy.CLAIMANT);
        settlement.acceptCourtDetermination(MadeBy.CLAIMANT);
        settlement.reject(MadeBy.DEFENDANT);

        return SampleClaim.builder().withSettlement(settlement).build();
    }

    private Claim buildClaimWithSettlementReached() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.builder().build(), MadeBy.DEFENDANT);
        settlement.accept(MadeBy.CLAIMANT);
        settlement.countersign(MadeBy.DEFENDANT);

        return SampleClaim.builder().withSettlement(settlement).withSettlementReachedAt(LocalDateTime.now()).build();
    }
}
