package uk.gov.hmcts.cmc.claimstore.tests.functional;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class SealedClaimPdfTest extends BaseTest {

    private User claimant;

    @Before
    public void before() {
        claimant = idamTestService.createCitizen();
    }


    @Test
    public void shouldBeAbleToFindTestClaimDataInClaimIssueReceiptPdf() throws IOException {
        Claim createdCase = getTestClaim();
        String pdfAsText = textContentOf(retrievePdf("claimIssueReceipt", createdCase.getExternalId()));
        assertionsOnClaimPdf(createdCase, pdfAsText);
    }

    @Test
    public void shouldBeAbleToFindTestClaimDataInSealedClaimPdf() throws IOException {
        Claim createdCase = getTestClaim();
        String pdfAsText = textContentOf(retrievePdf("sealedClaim", createdCase.getExternalId()));
        assertionsOnClaimPdf(createdCase, pdfAsText);
    }

    private void assertionsOnClaimPdf(Claim createdCase, String pdfAsText) {
        assertThat(pdfAsText).contains("Claim number: " + createdCase.getReferenceNumber());
        assertThat(pdfAsText).contains("Issued on: " + Formatting.formatDate(createdCase.getIssuedOn()));
        assertThat(pdfAsText).contains("Name: " + createdCase.getClaimData().getClaimant().getName());
        assertThat(pdfAsText).contains("Address: " +
            createdCase.getClaimData().getClaimant().getAddress().getLine1() + " \n" +
            createdCase.getClaimData().getClaimant().getAddress().getLine2() + " \n" +
            createdCase.getClaimData().getClaimant().getAddress().getLine3() + " \n" +
            createdCase.getClaimData().getClaimant().getAddress().getCity() + " \n" +
            createdCase.getClaimData().getClaimant().getAddress().getPostcode());
        assertThat(pdfAsText).contains("Name: " + createdCase.getClaimData().getDefendant().getName());
        assertThat(pdfAsText).contains("Address: " +
            createdCase.getClaimData().getDefendant().getAddress().getLine1() + " \n" +
            createdCase.getClaimData().getDefendant().getAddress().getLine2() + " \n" +
            createdCase.getClaimData().getDefendant().getAddress().getLine3() + " \n" +
            createdCase.getClaimData().getDefendant().getAddress().getCity() + " \n" +
            createdCase.getClaimData().getDefendant().getAddress().getPostcode());
        assertThat(pdfAsText).contains("Claim amount: " +
            Formatting.formatMoney(((AmountBreakDown) createdCase.getClaimData().getAmount()).getTotalAmount()));
        assertThat(pdfAsText).contains(Formatting.formatDate(createdCase.getResponseDeadline()));
    }

    private Claim getTestClaim() {
        ClaimData claimData = testData.submittedByClaimantBuilder()
            .build();

        return submitClaim(claimData)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);
    }

    private static String textContentOf(InputStream inputStream) throws IOException {
        PDDocument pdDocument = PDDocument.load(inputStream);

        try {
            return new PDFTextStripper().getText(pdDocument);
        } finally {
            pdDocument.close();
        }
    }

    private Response submitClaim(ClaimData claimData) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, claimant.getAuthorisation())
            .body(jsonMapper.toJson(claimData))
            .when()
            .post("/claims/" + claimant.getUserDetails().getId());
    }

    private InputStream retrievePdf(String pdfName, String externalId) {
        return RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, claimant.getAuthorisation())
            .get("/documents/" + pdfName + "/" + externalId)
            .asInputStream();
    }
}
