package uk.gov.hmcts.cmc.claimstore.tests.functional;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

public abstract class BasePdfTest extends BaseTest {
    protected User user;

    protected abstract void assertionsOnPdf(Claim createdCase, String pdfAsText);

    protected abstract Supplier<SampleClaimData> getSampleClaimDataBuilder();

    protected void shouldBeAbleToFindTestClaimDataInPdf(String pdfName) throws IOException {
        Claim createdCase = createCase();
        String pdfAsText = textContentOf(retrievePdf(pdfName, createdCase.getExternalId()));
        assertionsOnPdf(createdCase, pdfAsText);
    }

    protected Claim createCase() {
        ClaimData claimData = getSampleClaimDataBuilder().get().build();
        commonOperations.submitPrePaymentClaim(claimData.getExternalId().toString(), user.getAuthorisation());

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
            .header(HttpHeaders.AUTHORIZATION, user.getAuthorisation())
            .body(jsonMapper.toJson(claimData))
            .when()
            .post("/claims/" + user.getUserDetails().getId());
    }

    private InputStream retrievePdf(String pdfName, String externalId) {
        return RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, user.getAuthorisation())
            .get("/documents/" + pdfName + "/" + externalId)
            .asInputStream();
    }

    protected InputStream retrieveCCJPdf(String externalId) {
        return RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, user.getAuthorisation())
            .get("/documents/ccj/" + externalId)
            .asInputStream();
    }

    protected static String textContentOf(InputStream inputStream) throws IOException {
        PDDocument document = PDDocument.load(inputStream);
        try {
            return new PDFTextStripper().getText(document);
        } finally {
            document.close();
        }
    }

    protected static String getFullAddressString(Address address) {
        return address.getLine1() + " \n"
            + address.getLine2() + " \n"
            + address.getLine3() + " \n"
            + address.getCity() + " \n"
            + address.getPostcode();
    }
}
