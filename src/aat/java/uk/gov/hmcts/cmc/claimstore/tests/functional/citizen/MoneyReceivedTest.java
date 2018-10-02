package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.functional.BaseSubmitClaimTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class MoneyReceivedTest extends BaseSubmitClaimTest {

    private User claimant;

    @Before
    public void before() {
        claimant = idamTestService.createCitizen();
    }

    @Override
    protected Supplier<SampleClaimData> getSampleClaimDataBuilder() {
        return testData::submittedByClaimantBuilder;
    }

    @Test
    public void shouldSuccessfullySavePaidInFull() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        LocalDate moneyReceivedDate = LocalDate.now();

        Claim updatedCase = commonOperations
            .paidInFull(createdCase.getExternalId(), moneyReceivedDate, user)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);;

        assertThat(updatedCase.getMoneyReceivedOn()).isEqualTo(moneyReceivedDate);
    }

    @Test(expected = NotFoundException.class)
    public void shouldFailWhenClaimNotFound() {
        String claimantId = claimant.getUserDetails().getId();
        commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        LocalDate moneyReceivedDate = LocalDate.now();

        commonOperations
            .paidInFull( UUID.randomUUID().toString(), moneyReceivedDate, user)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

    }
}
