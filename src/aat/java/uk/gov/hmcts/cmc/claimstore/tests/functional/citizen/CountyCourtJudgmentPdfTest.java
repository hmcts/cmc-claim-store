package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.AmountContentProvider;
import uk.gov.hmcts.cmc.claimstore.tests.functional.BasePdfTest;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
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

public class CountyCourtJudgmentPdfTest extends BasePdfTest {

    @Autowired
    private AmountContentProvider amountContentProvider;

    private Claim claim;
    private User defendant;

    @Before
    public void before() {
        user = idamTestService.createCitizen();
        claim = createCase();
        modifyResponseDeadline(claim, user);
        //defendant = idamTestService.createDefendant(claim.getLetterHolderId());
        //commonOperations.linkDefendant(defendant.getAuthorisation());
    }

    @Test
    public void shouldBeAbleToFindDataInCCJByDefault() throws IOException {
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment
            .builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .build();

        claim = submitCCJ(countyCourtJudgment, false);
        String pdfAsText = textContentOf(retrieveCCJPdf(claim.getExternalId()));
        assertionsOnPdf(claim, pdfAsText);
    }

    @Test
    public void shouldBeAbleToFindDataInCCJIssuedRepaymentImmediatelyPdf() throws IOException {
        Response fullAdmissionResponse = SampleResponse.FullAdmission.builder().build();
        submitDefendantResponse(fullAdmissionResponse, claim.getExternalId());

        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment
            .builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .build();

        claim = submitCCJ(countyCourtJudgment, false);
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

        claim = submitCCJ(countyCourtJudgment, false);
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

        claim = submitCCJ(countyCourtJudgment, false);
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

        claim = submitCCJ(countyCourtJudgment, false);
        String pdfAsText = textContentOf(retrieveCCJPdf(claim.getExternalId()));
        assertionsOnPdf(claim, pdfAsText);
    }

    private void submitDefendantResponse(Response response, String externalId) {
        commonOperations.submitResponse(response, externalId, defendant)
            .then()
            .statusCode(HttpStatus.OK.value());

    }

    private Claim submitCCJ(CountyCourtJudgment countyCourtJudgment, boolean isIssue) {
        Claim claimReturnedAfterCCJIssued = commonOperations
            .requestCCJ(claim.getExternalId(), countyCourtJudgment, isIssue, user)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract()
            .body().as(Claim.class);

        assertThat(claimReturnedAfterCCJIssued.getCountyCourtJudgmentRequestedAt()).isNotNull();
        if(isIssue){
            assertTrue(claimReturnedAfterCCJIssued.getCountyCourtJudgmentIssuedAt().isPresent());
        }
        return claimReturnedAfterCCJIssued;
    }

    @Override
    protected Supplier<SampleClaimData> getSampleClaimDataBuilder() {
        return testData::submittedByClaimantBuilder;
    }

    @Override
    protected void assertionsOnPdf(Claim createdCase, String pdfAsText) {
        ClaimData claimData = createdCase.getClaimData();
        assertThat(pdfAsText).contains("Request for judgment by default");
        assertThat(pdfAsText).contains("In the County Court Business Centre");
        assertThat(pdfAsText).contains("Online Civil Money Claims");
        assertThat(pdfAsText).contains("Claim number: " + createdCase.getReferenceNumber());
        assertThat(pdfAsText).contains("Requested: " + formatDate(createdCase
            .getCountyCourtJudgmentRequestedAt()));
        assertThat(pdfAsText).contains("Claimant details");
        assertThat(pdfAsText).contains("Name: " + claimData.getClaimant().getName());
        assertThat(pdfAsText).contains("Address: " +
            getFullAddressString(claimData.getClaimant().getAddress()));
        claimData.getClaimant().getMobilePhone()
            .ifPresent(value -> assertThat(pdfAsText).contains("Telephone: " + value));
        assertThat(pdfAsText).contains("Defendant details");
        assertThat(pdfAsText).contains("Name: " + claimData.getDefendant().getName());
        assertThat(pdfAsText).contains("Address: " +
            getFullAddressString(claimData.getDefendant().getAddress()));
        claimData.getDefendant().getEmail().ifPresent(email -> assertThat(pdfAsText).contains("Email: " + email));
        assertThat(pdfAsText).contains("Claim amount details");
        assertThat(pdfAsText).contains("Claim amount: " +
            Formatting.formatMoney(((AmountBreakDown) createdCase.getClaimData().getAmount()).getTotalAmount()));

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

    private void modifyResponseDeadline(Claim caseCreated, User user) {
        LocalDate newResponseDeadline = caseCreated.getResponseDeadline().minusDays(60);
        String path = "/testing-support/claims/" + caseCreated.getReferenceNumber() + "/response-deadline/" + newResponseDeadline;
        RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, user.getAuthorisation())
            .when()
            .put(path).then().statusCode(HttpStatus.OK.value());
    }
}
