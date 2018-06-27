package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

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
import uk.gov.hmcts.cmc.domain.models.Address;
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
        Claim createdCase = createCase();
        String pdfAsText = textContentOf(RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, claimant.getAuthorisation())
            .get("/documents/" + "claimIssueReceipt" + "/" + createdCase.getExternalId())
            .asInputStream());
        assertionsOnClaimPdf(createdCase, pdfAsText);
    }

    @Test
    public void shouldBeAbleToFindTestClaimDataInSealedClaimPdf() throws IOException {
        Claim createdCase = createCase();
        String pdfAsText = textContentOf(RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, claimant.getAuthorisation())
            .get("/documents/" + "sealedClaim" + "/" + createdCase.getExternalId())
            .asInputStream());
        assertionsOnClaimPdf(createdCase, pdfAsText);
    }

    private static void assertionsOnClaimPdf(Claim createdCase, String pdfAsText) {
        assertThat(pdfAsText).contains("Claim number: " + createdCase.getReferenceNumber());
        assertThat(pdfAsText).contains("Issued on: " + Formatting.formatDate(createdCase.getIssuedOn()));
        assertThat(pdfAsText).contains("Name: " + createdCase.getClaimData().getClaimant().getName());
        assertThat(pdfAsText).contains("Address: "
            + getFullAddressString(createdCase.getClaimData().getClaimant().getAddress()));
        assertThat(pdfAsText).contains("Name: " + createdCase.getClaimData().getDefendant().getName());
        assertThat(pdfAsText).contains("Address: "
            + getFullAddressString(createdCase.getClaimData().getDefendant().getAddress()));
        assertThat(pdfAsText).contains("Claim amount: "
            + Formatting.formatMoney(((AmountBreakDown) createdCase.getClaimData().getAmount()).getTotalAmount()));
        assertThat(pdfAsText).contains(Formatting.formatDate(createdCase.getResponseDeadline()));
    }

    private static String getFullAddressString(Address address) {
        return address.getLine1() + " \n"
            + address.getLine2() + " \n"
            + address.getLine3() + " \n"
            + address.getCity() + " \n"
            + address.getPostcode();
    }

    private Claim createCase() {
        ClaimData claimData = testData.submittedByClaimantBuilder().build();
        commonOperations.submitPrePaymentClaim(claimData.getExternalId().toString(), claimant.getAuthorisation());

        return submitClaim(claimData)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);
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

    private static String textContentOf(InputStream inputStream) throws IOException {
        PDDocument document = PDDocument.load(inputStream);
        try {
            return new PDFTextStripper().getText(document);
        } finally {
            document.close();
        }
    }
}
