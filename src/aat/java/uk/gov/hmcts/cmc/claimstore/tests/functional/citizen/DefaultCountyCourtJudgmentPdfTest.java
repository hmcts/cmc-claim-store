package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.AmountContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;

import java.io.IOException;
import java.time.LocalDate;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultCountyCourtJudgmentPdfTest extends BaseCCJPdfTest {

    @Autowired
    private AmountContentProvider amountContentProvider;

    private Claim claim;

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
            .ccjType(CountyCourtJudgmentType.DEFAULT)
            .build();

        claim = submitCCJ(countyCourtJudgment);
        String pdfAsText = textContentOf(retrieveCCJPdf(claim.getExternalId()));
        assertionsOnPdf(claim, pdfAsText);
    }

    @Test
    public void shouldBeAbleToFindDataForRepaymentByInstalmentsPdf() throws IOException {
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment
            .builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .ccjType(CountyCourtJudgmentType.DEFAULT)
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .build();

        claim = submitCCJ(countyCourtJudgment);
        String pdfAsText = textContentOf(retrieveCCJPdf(claim.getExternalId()));
        assertionsOnPdf(claim, pdfAsText);
    }

    @Test
    public void shouldBeAbleToFindDataForSettledForLessAndRepaymentByInstalmentsPdf() throws IOException {

        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment
            .builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .ccjType(CountyCourtJudgmentType.DEFAULT)
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .build();

        claim = submitCCJ(countyCourtJudgment);
        String pdfAsText = textContentOf(retrieveCCJPdf(claim.getExternalId()));
        assertionsOnPdf(claim, pdfAsText);
    }

    @Test
    public void shouldBeAbleToFindDataForRepaymentBySetDatePdf() throws IOException {
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment
            .builder()
            .ccjType(CountyCourtJudgmentType.DEFAULT)
            .paymentOption(PaymentOption.BY_SPECIFIED_DATE)
            .payBySetDate(LocalDate.now().plusDays(20))
            .build();

        claim = submitCCJ(countyCourtJudgment);
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
        assertHeaderDetails(createdCase, pdfAsText, "Request for judgment by default");
        assertPartyAndContactDetails(claimData, pdfAsText);
        assertClaimAmountDetails(createdCase, pdfAsText);
        assertTotalAmount(createdCase, pdfAsText, amountContentProvider.create(createdCase));
        assertDeclaration(createdCase, pdfAsText);
    }

    private Claim submitCCJ(CountyCourtJudgment countyCourtJudgment) {
        Claim claimReturnedAfterCCJIssued = commonOperations
            .requestCCJ(claim.getExternalId(), countyCourtJudgment, user)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract()
            .body().as(Claim.class);

        assertThat(claimReturnedAfterCCJIssued.getCountyCourtJudgmentRequestedAt()).isNotNull();
        return claimReturnedAfterCCJIssued;
    }

    private void modifyResponseDeadline(Claim caseCreated, User user) {
        LocalDate newResponseDeadline = caseCreated.getResponseDeadline().minusDays(60);
        String path = "/testing-support/claims/" + caseCreated.getReferenceNumber()
            + "/response-deadline/" + newResponseDeadline;
        RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, user.getAuthorisation())
            .when()
            .put(path).then().statusCode(HttpStatus.OK.value());
    }
}
