package uk.gov.hmcts.cmc.domain.utils;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.BY_SPECIFIED_DATE;
import static uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention.bySetDate;

public class ClaimantResponseUtilsTest {

    @Test
    public void isCompanyOrOrganisationWithCCJDeterminationShouldBeTrue() {

        Claim claim = SampleClaim.builder()
            .withResponse(
                SampleResponse
                    .PartAdmission
                    .builder()
                    .buildWithPaymentOptionInstalmentsAndParty(SampleParty.builder().company()))
            .withClaimantResponse(ResponseAcceptation
                .builder()
                .courtDetermination(
                    CourtDetermination.builder()
                        .courtDecision(bySetDate())
                        .courtPaymentIntention(PaymentIntention.builder()
                            .paymentOption(BY_SPECIFIED_DATE)
                            .paymentDate(LocalDate.of(9999, 12, 31))
                            .build())
                        .disposableIncome(BigDecimal.valueOf(-1))
                        .decisionType(DecisionType.COURT)
                        .build())
                .formaliseOption(FormaliseOption.REFER_TO_JUDGE)
                .build())
            .build();

        ResponseAcceptation responseAcceptation =
            (ResponseAcceptation) claim.getClaimantResponse().orElseThrow(IllegalArgumentException::new);

        assertThat(ClaimantResponseUtils
            .isCompanyOrOrganisationWithCCJDetermination(claim, responseAcceptation)).isTrue();
    }

}
