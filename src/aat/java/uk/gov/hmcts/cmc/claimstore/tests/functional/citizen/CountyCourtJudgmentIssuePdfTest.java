package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.AmountContentProvider;
import uk.gov.hmcts.cmc.claimstore.tests.functional.BasePdfTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

public class CountyCourtJudgmentIssuePdfTest extends BasePdfTest {

    @Autowired
    private AmountContentProvider amountContentProvider;

    private Claim claim;
    private User defendant;

    @Before
    public void before() {
        user = idamTestService.createCitizen();
        claim = createCase();
        defendant = idamTestService.createDefendant(claim.getLetterHolderId());
        commonOperations.linkDefendant(defendant.getAuthorisation());
    }

    @Test
    public void shouldBeAbleToFindDataInCCJIssuedRepaymentImmediatelyPdf() throws IOException {
        Response fullAdmissionResponse = SampleResponse.FullAdmission.builder().build();
        submitDefendantResponse(fullAdmissionResponse, claim.getExternalId());

        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment
            .builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .build();

        claim = submitCCJByAdmission(countyCourtJudgment);
        String pdfAsText = textContentOf(retrieveCCJPdf(claim.getExternalId()));
        assertionsOnPdf(claim, pdfAsText);
    }

    @Test
    public void shouldBeAbleToFindDataInCCJIssuedRepaymentByInstalmentsPdf() throws IOException {
        Response fullAdmissionResponse = SampleResponse.FullAdmission.builder().build();
        submitDefendantResponse(fullAdmissionResponse, claim.getExternalId());

        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment
            .builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .build();

        claim = submitCCJByAdmission(countyCourtJudgment);
        String pdfAsText = textContentOf(retrieveCCJPdf(claim.getExternalId()));
        assertionsOnPdf(claim, pdfAsText);
    }

    @Test
    public void shouldBeAbleToFindDataInCCJIssuedSettledForLessAndRepaymentByInstalmentsPdf() throws IOException {
        Response partAdmissionResponse = SampleResponse.PartAdmission.builder().buildWithPaymentOptionBySpecifiedDate();
        submitDefendantResponse(partAdmissionResponse, claim.getExternalId());

        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment
            .builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .build();

        claim = submitCCJByAdmission(countyCourtJudgment);
        String pdfAsText = textContentOf(retrieveCCJPdf(claim.getExternalId()));
        assertionsOnPdf(claim, pdfAsText);
    }

    @Test
    public void shouldBeAbleToFindDataInCCJIssuedRepaymentBySetDatePdf() throws IOException {
        Response fullAdmissionResponse = SampleResponse.FullAdmission.builder().build();
        submitDefendantResponse(fullAdmissionResponse, claim.getExternalId());

        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment
            .builder()
            .paymentOption(PaymentOption.BY_SPECIFIED_DATE)
            .payBySetDate(LocalDate.now().plusDays(20))
            .build();

        claim = submitCCJByAdmission(countyCourtJudgment);
        String pdfAsText = textContentOf(retrieveCCJPdf(claim.getExternalId()));
        assertionsOnPdf(claim, pdfAsText);
    }

    private void submitDefendantResponse(Response response, String externalId) {
        commonOperations.submitResponse(response, externalId, defendant)
            .then()
            .statusCode(HttpStatus.OK.value());

    }

    private Claim submitCCJByAdmission(CountyCourtJudgment countyCourtJudgment) {
        Claim claimReturnedAfterCCJIssued = commonOperations
            .requestCCJ(claim.getExternalId(), countyCourtJudgment, true, user)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract()
            .body().as(Claim.class);

        assertTrue(claimReturnedAfterCCJIssued.getCountyCourtJudgmentIssuedAt().isPresent());

        return claimReturnedAfterCCJIssued;
    }

    @Override
    protected Supplier<SampleClaimData> getSampleClaimDataBuilder() {
        return testData::submittedByClaimantBuilder;
    }

    @Override
    protected void assertionsOnPdf(Claim createdCase, String pdfAsText) {
        assertThat(pdfAsText).contains("Judgment by admission");
        assertThat(pdfAsText).contains("Claim number: " + createdCase.getReferenceNumber());
        assertThat(pdfAsText).contains("Date of order: " + formatDate(createdCase
            .getCountyCourtJudgmentIssuedAt()
            .orElseThrow(IllegalArgumentException::new)));
        assertThat(pdfAsText).contains("Claimant name: " + createdCase.getClaimData().getClaimant().getName());
        assertThat(pdfAsText).contains("Defendant name: " + createdCase.getClaimData().getDefendant().getName());

        String amountToBePaid = amountContentProvider.create(createdCase).getRemainingAmount();
        assertThat(pdfAsText).contains("It is ordered that you must pay the claimant " + amountToBePaid
            + " for debt and interest to date of");

        CountyCourtJudgment countyCourtJudgment = createdCase.getCountyCourtJudgment();
        switch (countyCourtJudgment.getPaymentOption()) {
            case IMMEDIATELY:
                assertThat(pdfAsText).contains("You must pay the claimant the total of " + amountToBePaid
                    + " forthwith.");
                break;
            case BY_SPECIFIED_DATE:
                LocalDate bySetDate = countyCourtJudgment.getPayBySetDate().orElseThrow(IllegalArgumentException::new);
                assertThat(pdfAsText).contains(
                    String.format("You must pay the claimant the total of %s by %s",
                        amountToBePaid,
                        formatDate(bySetDate)
                    )
                );
                break;
            case INSTALMENTS:
                RepaymentPlan repaymentPlan = countyCourtJudgment.getRepaymentPlan()
                    .orElseThrow(IllegalStateException::new);
                String instalmentAmount = formatMoney(repaymentPlan.getInstalmentAmount());
                assertThat(pdfAsText).contains(String.format("You must pay by instalments of %s every week",
                    instalmentAmount));
                String firstPaymentDate = formatDate(repaymentPlan.getFirstPaymentDate());
                String paymentScheduleLine1 = "The first payment must reach the claimant by %s and on or before this";
                assertThat(pdfAsText).contains(String.format(paymentScheduleLine1, firstPaymentDate));
                String paymentScheduleLine2 = "date every week until the debt has been paid.";
                assertThat(pdfAsText).contains(paymentScheduleLine2);
                break;
            default:
                throw new NotFoundException(countyCourtJudgment.getPaymentOption() + "not found");
        }

    }
}
