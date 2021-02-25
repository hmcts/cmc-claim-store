package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.Retry;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.RetryFailedFunctionalTests;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseMethod;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class OCON9XResponeTest extends BaseTest {

    private User claimant;
    Claim createdCase;

    @Before
    public void before() {
        claimant = bootstrap.getClaimant();

        createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimant.getUserDetails().getId()
        );

    }

    @Rule
    public RetryFailedFunctionalTests retryRule = new RetryFailedFunctionalTests(3);

    @Test
    @Retry
    public void shouldBeAbleToSuccessfullyAdmitTheClaimThroughOCON9X() {
        String defendantCollectionId = UUID.randomUUID().toString();

        User defendant = idamTestService.upliftDefendant(createdCase.getLetterHolderId(), bootstrap.getDefendant());
        commonOperations.linkDefendant(defendant.getAuthorisation(), createdCase.getLetterHolderId());
        Response fullAdmissionResponse = FullAdmissionResponse.builder()
            .moreTimeNeeded(YesNoOption.NO)
            .defendant(SampleParty.builder()
                .withCollectionId(defendantCollectionId)
                .individual())
            .paymentIntention(SamplePaymentIntention.instalments())
            .responseMethod(ResponseMethod.OCON_FORM).build();

        Claim updatedCase = commonOperations
            .submitResponse(fullAdmissionResponse, createdCase.getExternalId(), defendant)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);
        assertThat(updatedCase.getResponse().isPresent()).isTrue();
        assertThat(updatedCase.getResponse().get().getResponseMethod()).isEqualTo(ResponseMethod.OCON_FORM);
        assertThat(updatedCase.getRespondedAt()).isNotNull();
    }

    @Test
    @Retry
    public void shouldBeAbleToSuccessfullyDisputeThroughOCON9X() {
        String defendantCollectionId = UUID.randomUUID().toString();

        User defendant = idamTestService.upliftDefendant(createdCase.getLetterHolderId(), bootstrap.getDefendant());
        commonOperations.linkDefendant(defendant.getAuthorisation(), createdCase.getLetterHolderId());
        Response defenceResponse = FullDefenceResponse.builder()
            .moreTimeNeeded(YesNoOption.NO)
            .defendant(SampleParty.builder()
                .withCollectionId(defendantCollectionId)
                .individual())
            .defenceType(DefenceType.DISPUTE)
            .responseMethod(ResponseMethod.OCON_FORM).build();

        Claim updatedCase = commonOperations.submitResponse(defenceResponse, createdCase.getExternalId(), defendant)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);
        assertThat(updatedCase.getResponse().isPresent()).isTrue();
        assertThat(updatedCase.getResponse().get().getResponseMethod()).isEqualTo(ResponseMethod.OCON_FORM);
        assertThat(updatedCase.getRespondedAt()).isNotNull();
    }

}
