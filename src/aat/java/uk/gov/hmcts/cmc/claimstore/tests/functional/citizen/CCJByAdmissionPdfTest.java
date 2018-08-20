package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.functional.BaseClaimPdfTest;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.io.IOException;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

public class CCJByAdmissionPdfTest extends BaseClaimPdfTest {

    @Before
    public void before() {
        user = idamTestService.createCitizen();
    }

    @Test
    public void shouldBeAbleToFindAppropriateDataInCCJByAdmissionPdf() throws IOException {
        Claim createdCase = createCase();
        String externalId = createdCase.getExternalId();
        User defendant = idamTestService.createDefendant(createdCase.getLetterHolderId());
        submitResponse(createdCase.getExternalId(), defendant.getUserDetails().getId());
        submitCCJByAdmission(externalId);
        String pdfAsText = textContentOf(retrieveCCJPdf(externalId));
        assertionsOnPdf(createdCase, pdfAsText);
    }

    private void submitResponse(String externalId, String defendantId) {
        Response fullAdmissionResponse = SampleResponse.FullAdmission.builder().build();
        RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, user.getAuthorisation())
            .body(jsonMapper.toJson(fullAdmissionResponse))
            .when()
            .post("/responses/claim/" + externalId + "/defendant/" + defendantId);
    }

    private void submitCCJByAdmission(String externalId) {
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment
            .builder()
            .withPaymentOptionImmediately()
            .build();

        RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, user.getAuthorisation())
            .body(jsonMapper.toJson(countyCourtJudgment))
            .when()
            .post("/claims/" + externalId + "/county-court-judgment?issue=true");
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
        assertThat(pdfAsText).contains("Claimant Name: " + createdCase.getClaimData().getClaimant().getName());
        assertThat(pdfAsText).contains("Defendant name: " + createdCase.getClaimData().getDefendant().getName());
        String amountToBePaid = formatMoney(
                                createdCase
                                    .getAmountWithInterest()
                                .orElseThrow(IllegalStateException::new)
                                );
        assertThat(pdfAsText).contains("It is ordered that you must pay the claimant " +  amountToBePaid
            + " for debt and interest to date of judgment");
    }
}
