package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.AmountContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.AmountContentProvider;
import uk.gov.hmcts.cmc.claimstore.tests.functional.BasePdfTest;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;

import java.io.IOException;
import java.time.LocalDate;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public class DefaultCountyCourtJudgmentPdfTest extends BasePdfTest {

    @Autowired
    private AmountContentProvider amountContentProvider;

    private Claim claim;
    private User defendant;

    @Before
    public void before() {
        user = idamTestService.createCitizen();
        claim = createCase();
        modifyResponseDeadline(claim, user);
    }

    @Test
    public void shouldBeAbleToFindDataForRepaymentImmediatelyPdf() throws IOException {
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment
            .builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .build();

        claim = submitCCJ(countyCourtJudgment, false);
        String pdfAsText = textContentOf(retrieveCCJPdf(claim.getExternalId()));
        assertionsOnPdf(claim, pdfAsText);
    }

    @Test
    public void shouldBeAbleToFindDataForRepaymentByInstalmentsPdf() throws IOException {
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
    public void shouldBeAbleToFindDataForSettledForLessAndRepaymentByInstalmentsPdf() throws IOException {

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
    public void shouldBeAbleToFindDataForRepaymentBySetDatePdf() throws IOException {
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment
            .builder()
            .paymentOption(PaymentOption.BY_SPECIFIED_DATE)
            .payBySetDate(LocalDate.now().plusDays(20))
            .build();

        claim = submitCCJ(countyCourtJudgment, false);
        String pdfAsText = textContentOf(retrieveCCJPdf(claim.getExternalId()));
        assertionsOnPdf(claim, pdfAsText);
    }

    @Override
    protected Supplier<SampleClaimData> getSampleClaimDataBuilder() {
        return testData::submittedByClaimantBuilder;
    }

    @Override
    protected void assertionsOnPdf(Claim createdCase, String pdfAsText) {
        ClaimData claimData = createdCase.getClaimData();
        AmountContent amountContent = amountContentProvider.create(createdCase);
        assertHeaderDetails(createdCase, pdfAsText);
        assertPartyAndContactDetails(createdCase, pdfAsText, claimData);
        assertClaimAmountDetails(createdCase, pdfAsText);
        assertTotalAmount(createdCase, pdfAsText, amountContent);
        assertDeclaration(createdCase, pdfAsText);
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
        if (isIssue) {
            assertTrue(claimReturnedAfterCCJIssued.getCountyCourtJudgmentIssuedAt().isPresent());
        }
        return claimReturnedAfterCCJIssued;
    }

    private void assertHeaderDetails(Claim createdCase, String pdfAsText) {
        assertThat(pdfAsText).contains("Request for judgment by default");
        assertThat(pdfAsText).contains("In the County Court Business Centre");
        assertThat(pdfAsText).contains("Online Civil Money Claims");
        assertThat(pdfAsText).contains("Claim number: " + createdCase.getReferenceNumber());
        assertThat(pdfAsText).contains("Requested: " + formatDate(createdCase.getCountyCourtJudgmentRequestedAt()));
    }

    private void assertPartyAndContactDetails(Claim createdCase, String pdfAsText, ClaimData claimData) {
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
        claimData.getDefendant().getEmail().ifPresent(value -> assertThat(pdfAsText).contains("Email:"));
    }

    private void assertClaimAmountDetails(Claim createdCase, String pdfAsText) {
        ClaimData claimData = createdCase.getClaimData();
        assertThat(pdfAsText).contains("Claim amount details");
        assertThat(pdfAsText).contains("Claim amount: " +
            Formatting.formatMoney(((AmountBreakDown) claimData.getAmount()).getTotalAmount()));
        assertInterest(claimData, pdfAsText);
        CountyCourtJudgment countyCourtJudgment = createdCase.getCountyCourtJudgment();
        PaymentOption paymentOption = countyCourtJudgment.getPaymentOption();
        switch (paymentOption) {
            case IMMEDIATELY:
                assertThat(pdfAsText).contains("When you want the \n" +
                    "defendant to pay: " + paymentOption.getDescription());
                break;
            case INSTALMENTS:
            case BY_SPECIFIED_DATE:
                assertThat(pdfAsText).contains("How you want the \n" +
                    "defendant to pay: " + paymentOption.getDescription());
                break;
            default:
                throw new IllegalArgumentException("Wrong payment option: " + paymentOption.name());

        }
    }

    private void assertInterest(ClaimData claimData, String pdfAsText) {
        Interest.InterestType interestType = claimData.getInterest().getType();
        switch (interestType) {
            case NO_INTEREST:
                assertThat(pdfAsText.contains("Interest: No interest claimed"));
                break;
            case STANDARD:
            case DIFFERENT:
                assertThat(pdfAsText.contains("Interest rate claimed:" + claimData.getInterest().getRate()));
                break;
            case BREAKDOWN:
                assertThat(pdfAsText.contains("Total interest amount:" + claimData
                    .getInterest()
                    .getInterestBreakdown()
                    .getTotalAmount()));
            default:
                throw new IllegalArgumentException("Incorrect interest type: " + interestType);
        }
    }

    private void assertTotalAmount(Claim createdCase, String pdfAsText, AmountContent amountContent) {
        ClaimData claimData = createdCase.getClaimData();

        assertThat(pdfAsText).contains("Total amount");
        assertThat(pdfAsText).contains("Claim amount: " +
            Formatting.formatMoney(((AmountBreakDown) claimData.getAmount()).getTotalAmount()));
        assertInterest(claimData, pdfAsText);
        assertThat(pdfAsText).contains("Claim fee: " + amountContent.getFeeAmount());
        assertThat(pdfAsText).contains("Subtotal: " + amountContent.getSubTotalAmount());
        assertThat(pdfAsText).contains("Amount already paid by \n" +
            "defendant: " + amountContent.getPaidAmount());
        assertThat(pdfAsText).contains("Total: " + amountContent.getRemainingAmount());

    }

    private void assertDeclaration(Claim createdCase, String pdfAsText) {
        assertThat(pdfAsText).contains("Declaration");
        assertThat(pdfAsText).contains("I declare that the details I have given are true to the \n" +
            "best of my knowledge.");
        assertThat(pdfAsText).contains(createdCase.getClaimData().getClaimant().getName());
        assertThat(pdfAsText).contains(formatDate(createdCase.getCountyCourtJudgmentRequestedAt()));
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
