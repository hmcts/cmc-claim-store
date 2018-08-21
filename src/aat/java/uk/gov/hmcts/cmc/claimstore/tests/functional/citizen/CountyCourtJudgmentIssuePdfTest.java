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
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
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

public class CountyCourtJudgmentIssuePdfTest extends BasePdfTest {

    @Autowired
    private AmountContentProvider amountContentProvider;

    private Claim claim;

    @Before
    public void before() {
        user = idamTestService.createCitizen();
        claim = createCase();
        User defendant = idamTestService.createDefendant(claim.getLetterHolderId());
        linkDefendant(defendant.getAuthorisation());
        submitDefendantResponse(claim.getExternalId(), defendant.getUserDetails().getId());
    }

    @Test
    public void shouldBeAbleToFindDataInCCJIssuedRepaymentImmediatelyPdf() throws IOException {
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment
            .builder()
            .withPaymentOptionImmediately()
            .build();

        claim = submitCCJByAdmission(countyCourtJudgment);
        String pdfAsText = textContentOf(retrieveCCJPdf(claim.getExternalId()));
        assertionsOnPdf(claim, pdfAsText);
    }

    @Test
    public void shouldBeAbleToFindDataInCCJIssuedRepaymentByInstalmentsPdf() throws IOException {
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment
            .builder()
            .withRepaymentPlan(SampleRepaymentPlan.builder().build())
            .build();

        claim = submitCCJByAdmission(countyCourtJudgment);
        String pdfAsText = textContentOf(retrieveCCJPdf(claim.getExternalId()));
        assertionsOnPdf(claim, pdfAsText);
    }

    @Test
    public void shouldBeAbleToFindDataInCCJIssuedRepaymentBySetDatePdf() throws IOException {
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment
            .builder()
            .withPayBySetDate(LocalDate.now().plusDays(20))
            .build();

        claim = submitCCJByAdmission(countyCourtJudgment);
        String pdfAsText = textContentOf(retrieveCCJPdf(claim.getExternalId()));
        assertionsOnPdf(claim, pdfAsText);
    }


    private void linkDefendant(String authorisation) {
        RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, authorisation)
            .when()
            .put("claims/defendant/link")
            .then()
            .statusCode(HttpStatus.OK.value());

    }

    private void submitDefendantResponse(String externalId, String defendantId) {
        Response fullAdmissionResponse = SampleResponse.FullAdmission.builder().build();
        RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, user.getAuthorisation())
            .body(jsonMapper.toJson(fullAdmissionResponse))
            .when()
            .post("/responses/claim/" + externalId + "/defendant/" + defendantId)
            .then()
            .statusCode(HttpStatus.OK.value());

    }

    private Claim submitCCJByAdmission(CountyCourtJudgment countyCourtJudgment) {
        Claim claimReturnedAfterCCJIssued = RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, user.getAuthorisation())
            .queryParam("issue", true)
            .body(jsonMapper.toJson(countyCourtJudgment))
            .when()
            .post("/claims/" + claim.getExternalId() + "/county-court-judgment").as(Claim.class);

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
        assertThat(pdfAsText).contains("Date of order: " + Formatting.formatDate(createdCase
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
                String instalmentAmount = Formatting.formatMoney(repaymentPlan.getInstalmentAmount());
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
