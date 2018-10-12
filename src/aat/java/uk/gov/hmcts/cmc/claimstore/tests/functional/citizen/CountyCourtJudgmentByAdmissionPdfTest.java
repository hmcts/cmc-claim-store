package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.AmountContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.AmountContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.function.Supplier;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CountyCourtJudgmentByAdmissionPdfTest extends BaseCCJPdfTest {

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

        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .buildAcceptationIssueCCJWithDefendantPaymentIntention();

        io.restassured.response.Response responseReceived = commonOperations
            .submitClaimantResponse(claimantResponse, claim.getExternalId(), user);
        responseReceived.then().statusCode(HttpStatus.OK.value());

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
        Response partAdmissionResponse = SampleResponse.PartAdmission.builder().build();
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
        ClaimData claimData = createdCase.getClaimData();
        AmountContent amountContent = amountContentProvider.create(createdCase);
        assertHeaderDetails(createdCase, pdfAsText, "Judgment by admission");
        assertPartyAndContactDetails(claimData, pdfAsText);
        assertClaimAmountDetails(createdCase, pdfAsText);
        assertTotalAmount(createdCase, pdfAsText, amountContent);
        assertDeclaration(createdCase, pdfAsText);
    }
}
