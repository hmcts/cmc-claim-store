package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.BY_SPECIFIED_DATE;
import static uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention.bySetDate;

public class ResponseAcceptationContentProviderTest {

    private final PaymentIntentionContentProvider paymentIntentionContentProvider
        = new PaymentIntentionContentProvider();

    private final ResponseAcceptationContentProvider contentProvider =
        new ResponseAcceptationContentProvider(paymentIntentionContentProvider);

    @Before
    public void setup() {
        paymentIntentionContentProvider.createContent(
            PaymentOption.IMMEDIATELY,
            null,
            LocalDate.now(),
            null,
            null
        );

    }

    @Test
    public void shouldGetContentSayingDisposableIncomeIsNegativeAndSelectDefendantRepaymentPlan() {
        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse
                .PartAdmission
                .builder()
                .buildWithPaymentOptionInstalmentsAndParty(SampleParty.builder().company()))
            .withClaimantResponse(ResponseAcceptation.builder()
                .courtDetermination(CourtDetermination.builder()
                    .courtDecision(bySetDate())
                    .courtPaymentIntention(PaymentIntention.builder()
                        .paymentOption(BY_SPECIFIED_DATE)
                        .paymentDate(ResponseAcceptationContentProvider.SYSTEM_MAX_DATE)
                        .build())
                    .disposableIncome(BigDecimal.valueOf(-1))
                    .decisionType(DecisionType.COURT)
                    .build())
                .build())
            .build();

        Map<String, Object> content = contentProvider.createContent(claim);

        assertThat(content).containsKeys("hasNegativeDisposableIncome");
        assertThat(content).containsValue("The defendant’s disposable income is -£1."
            + " As such, the court has selected the defendant’s repayment plan.");
    }

    @Test
    public void shouldExcludeCourtDeterminationSectionWhenReferToJudgeForCompanyOrOrganisation() {

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
                           .paymentDate(ResponseAcceptationContentProvider.SYSTEM_MAX_DATE)
                           .build())
                       .disposableIncome(BigDecimal.valueOf(-1))
                       .decisionType(DecisionType.COURT)
                       .build())
                .formaliseOption(FormaliseOption.REFER_TO_JUDGE)
                .build())
            .build();

        Map<String, Object> content = contentProvider.createContent(claim);

        assertThat(content).doesNotContainKey("courtDetermination");
    }

    @Test
    public void shouldCourtDeterminationSectionWhenReferToJudgeForIndividuals() {

        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withDefendant(SampleTheirDetails.builder().individualDetails())
                .build())
            .withResponse(SampleResponse.PartAdmission.builder().buildWithPaymentOptionInstalments())
            .withClaimantResponse(ResponseAcceptation.builder()
                .courtDetermination(CourtDetermination.builder()
                    .courtDecision(bySetDate())
                    .courtPaymentIntention(SamplePaymentIntention.instalments())
                    .disposableIncome(BigDecimal.valueOf(10))
                    .decisionType(DecisionType.COURT)
                    .build())
                .formaliseOption(FormaliseOption.REFER_TO_JUDGE)
                .build())
            .build();

        Map<String, Object> content = contentProvider.createContent(claim);

        assertThat(content).containsKey("courtDetermination");
    }
}
