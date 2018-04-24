package uk.gov.hmcts.cmc.claimstore.tests.functional;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class SealedClaimPdfTest extends BaseTest {

    private User claimant;
    @Autowired
    private ClaimIssueReceiptService claimIssueReceiptService;
    static File fileName = new File("/Users/kiranv/cmc/claim-store/src/aat/java/uk/gov/hmcts/cmc/claimstore/tests/functional/999MC042-claim-form.pdf");

    @Before
    public void before() {
        claimant = idamTestService.createCitizen();
    }

    private static String textContentOf(File file) throws IOException {
        PDDocument pdDocument = PDDocument.load(file);

        try {
            return new PDFTextStripper().getText(pdDocument);
        } finally {
            pdDocument.close();
        }
    }

    @Test
    public void shouldCreateClaimWithExpectedClaimNumber() throws IOException {
//        System.out.println(textContentOf(new File(fileName)));

        ClaimData claimData = testData.submittedByClaimantBuilder()
            .build();

        Claim createdCase = submitClaim(claimData)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        byte[] getPdf = claimIssueReceiptService.createPdf(createdCase);
        System.out.println(getPdf);

        assertThat(textContentOf(fileName).contains("999MC042"));
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


}
