package uk.gov.hmcts.cmc.claimstore.tests.functional.solicitor;

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
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class SealedClaimPdfAsSolicitorTest extends BaseSolicitorTest {
    private User solicitor;

    @Before
    public void before() {
        solicitor = idamTestService.createSolicitor();
    }

    @Test
    public void shouldBeAbleToFindTestClaimDataInSealedClaimPdf() throws IOException {
        Claim createdCase = createCase();
        String pdfAsText = textContentOf(RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, solicitor.getAuthorisation())
            .get("/documents/sealedClaim/" + createdCase.getExternalId())
            .asInputStream());
        assertionsOnClaimPdf(createdCase, pdfAsText);
    }

    private static void assertionsOnClaimPdf(Claim createdCase, String pdfAsText) {
        ClaimData claimData = createdCase.getClaimData();
        Party claimant = claimData.getClaimant();
        assertThat(pdfAsText).contains("Claim number: " + createdCase.getReferenceNumber());
        assertThat(pdfAsText).contains("Fee account: " + claimData.getFeeAccountNumber().get());
        assertThat(pdfAsText).contains("Claim issued: " + Formatting.formatDate(createdCase.getIssuedOn()));
        assertThat(pdfAsText).contains("Claimant " + claimant.getName() + " \n"
            + getFullAddressString(claimant.getAddress()));
        assertThat(pdfAsText).contains("Service address " + claimData.getDefendant().getName() + " \n"
            + getFullAddressString(claimant.getCorrespondenceAddress().get()));
        assertThat(pdfAsText).contains("The claimant expects to recover up to "
            + Formatting.formatMoney(((AmountRange) claimData.getAmount()).getHigherValue()));
    }

    private static String getFullAddressString(Address address) {
        return address.getLine1() + " \n"
            + address.getLine2() + " \n"
            // line 3 is not used
            + address.getCity() + " \n"
            + address.getPostcode();
    }

    private Claim createCase() {
        ClaimData claimData = testData.submittedBySolicitorBuilder().build();
        commonOperations.submitPrePaymentClaim(claimData.getExternalId().toString(), solicitor.getAuthorisation());
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
            .header(HttpHeaders.AUTHORIZATION, solicitor.getAuthorisation())
            .body(jsonMapper.toJson(claimData))
            .when()
            .post("/claims/" + solicitor.getUserDetails().getId());
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
