package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.AmountContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.io.IOException;
import java.util.function.Supplier;

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
    public void shouldBeAbleToFindDataInDefendantRepaymentImmediately() throws IOException {
        Response fullAdmissionResponse = SampleResponse.FullAdmission.builder().buildWithPaymentOptionImmediately();

        submitDefendantResponse(fullAdmissionResponse, claim.getExternalId());

        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .buildAcceptationIssueCCJWithDefendantPaymentIntention();

        io.restassured.response.Response responseReceived = commonOperations
            .submitClaimantResponse(claimantResponse, claim.getExternalId(), user);
        responseReceived.then().statusCode(HttpStatus.CREATED.value());

        String pdfAsText = textContentOf(retrieveCCJPdf(claim));

        assertionsOnPdf(claim, pdfAsText);
    }

    @Test
    public void shouldBeAbleToFindDataFullAdmitByClaimantPaymentIntentionBySetDate() throws IOException {
        Response fullAdmissionResponse = SampleResponse.FullAdmission.builder().build();

        submitDefendantResponse(fullAdmissionResponse, claim.getExternalId());

        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .buildAcceptationIssueCCJWithCourtDetermination();

        io.restassured.response.Response responseReceived = commonOperations
            .submitClaimantResponse(claimantResponse, claim.getExternalId(), user);
        responseReceived.then().statusCode(HttpStatus.CREATED.value());

        String pdfAsText = textContentOf(retrieveCCJPdf(claim));

        assertionsOnPdf(claim, pdfAsText);
    }

    @Test
    public void shouldBeAbleToFindDataFullAdmitByClaimantPaymentIntentionByRepaymentPlan() throws IOException {
        Response fullAdmissionResponse = SampleResponse.FullAdmission.builder().build();

        submitDefendantResponse(fullAdmissionResponse, claim.getExternalId());

        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .buildAcceptationIssueCCJWithDefendantPaymentIntention();

        io.restassured.response.Response responseReceived = commonOperations
            .submitClaimantResponse(claimantResponse, claim.getExternalId(), user);
        responseReceived.then().statusCode(HttpStatus.CREATED.value());

        String pdfAsText = textContentOf(retrieveCCJPdf(claim));

        assertionsOnPdf(claim, pdfAsText);
    }

    @Test
    public void shouldBeAbleToFindDataInPartAdmitByDefendantPayingBySetDate() throws IOException {
        Response partAdmissionResponse = SampleResponse.PartAdmission.builder().buildWithPaymentOptionBySpecifiedDate();

        submitDefendantResponse(partAdmissionResponse, claim.getExternalId());

        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .buildAcceptationIssueCCJWithDefendantPaymentIntention();

        io.restassured.response.Response responseReceived = commonOperations
            .submitClaimantResponse(claimantResponse, claim.getExternalId(), user);
        responseReceived.then().statusCode(HttpStatus.CREATED.value());

        String pdfAsText = textContentOf(retrieveCCJPdf(claim));

        assertionsOnPdf(claim, pdfAsText);
    }

    @Test
    public void shouldBeAbleToFindDataInPartAdmitAndRepaymentByInstalmentsPdf() throws IOException {
        Response partAdmissionResponse = SampleResponse.PartAdmission.builder().buildWithPaymentOptionInstalments();
        submitDefendantResponse(partAdmissionResponse, claim.getExternalId());

        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .buildAcceptationIssueCCJWithDefendantPaymentIntention();

        io.restassured.response.Response responseReceived = commonOperations
            .submitClaimantResponse(claimantResponse, claim.getExternalId(), user);
        responseReceived.then().statusCode(HttpStatus.CREATED.value());

        String pdfAsText = textContentOf(retrieveCCJPdf(claim));

        assertionsOnPdf(claim, pdfAsText);
    }

    @Test
    public void shouldBeAbleToFindDataPartAdmitWithCourtPaymentIntentionRepaymentBySetDatePdf() throws IOException {
        Response partAdmissionResponse = SampleResponse.PartAdmission.builder().buildWithPaymentOptionBySpecifiedDate();
        submitDefendantResponse(partAdmissionResponse, claim.getExternalId());

        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .buildAcceptationIssueCCJWithCourtDetermination();

        io.restassured.response.Response responseReceived = commonOperations
            .submitClaimantResponse(claimantResponse, claim.getExternalId(), user);
        responseReceived.then().statusCode(HttpStatus.CREATED.value());

        String pdfAsText = textContentOf(retrieveCCJPdf(claim));

        assertionsOnPdf(claim, pdfAsText);
    }

    private void submitDefendantResponse(Response response, String externalId) {
        commonOperations.submitResponse(response, externalId, defendant)
            .then()
            .statusCode(HttpStatus.OK.value());
    }

    @Override
    protected Supplier<SampleClaimData> getSampleClaimDataBuilder() {
        return testData::submittedByClaimantBuilder;
    }

    @Override
    protected void assertionsOnPdf(Claim createdCase, String pdfAsText) {
        Claim retrievedCase = commonOperations.retrieveClaim(createdCase.getExternalId(), user.getAuthorisation());
        ClaimData claimData = retrievedCase.getClaimData();
        assertHeaderDetails(retrievedCase, pdfAsText, "Judgment by admission");
        assertPartyAndContactDetails(claimData, pdfAsText);
        assertClaimAmountDetails(retrievedCase, pdfAsText);
        assertTotalAmount(retrievedCase, pdfAsText, amountContentProvider.create(retrievedCase));
        assertDeclaration(retrievedCase, pdfAsText);
    }
}
