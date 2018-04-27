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
        assertThat(pdfAsText.contains(createdCase.getReferenceNumber())).isTrue();
        assertThat(pdfAsText.contains("Issued on: " + Formatting.formatDate(createdCase.getIssuedOn()))).isTrue();
        assertThat(pdfAsText.contains("Name: " + createdCase.getClaimData().getClaimant().getName())).isTrue();
        assertThat(pdfAsText.contains(createdCase.getClaimData().getClaimant().getAddress().getPostcode())).isTrue();
        assertThat(pdfAsText.contains("Name: " + createdCase.getClaimData().getDefendant().getName())).isTrue();
        assertThat(pdfAsText.contains(createdCase.getClaimData().getDefendant().getAddress().getPostcode())).isTrue();
//        assertThat(pdfAsText.contains(Formatting.formatMoney(createdCase.getClaimData().getAmount()))).isTrue();
        assertThat(pdfAsText.contains(Formatting.formatDate(createdCase.getResponseDeadline()))).isTrue();
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
