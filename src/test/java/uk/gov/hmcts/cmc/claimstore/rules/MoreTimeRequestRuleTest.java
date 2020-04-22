package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeAlreadyRequestedException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MoreTimeRequestRuleTest {

    private MoreTimeRequestRule moreTimeRequestRule;

    @Before
    public void beforeEachTest() {
        moreTimeRequestRule = new MoreTimeRequestRule();
    }

    @Test
    public void noExceptionThrownWhenMoreTimeRequestedFirstTimeAnDefendantNotResponded() {
        Claim claim = SampleClaim.builder()
            .withClaimantRespondedAt(null)
            .withMoreTimeRequested(false)
            .build();
        assertThatCode(() -> moreTimeRequestRule.assertMoreTimeCanBeRequested(claim)).doesNotThrowAnyException();
    }

    @Test(expected = MoreTimeAlreadyRequestedException.class)
    public void shouldThrowExceptionWhenMoreTimeWasAlreadyRequested() {
        Claim claim = SampleClaim.builder()
            .withMoreTimeRequested(true)
            .build();
        moreTimeRequestRule.assertMoreTimeCanBeRequested(claim);
    }

    @Test
    public void shouldReturnValidationErrorWhenAlreadyRequestedMoreTime() {
        Claim claim = SampleClaim.builder()
                .withMoreTimeRequested(true)
                .build();
        List<String> errors = moreTimeRequestRule.validateMoreTimeCanBeRequested(claim);
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors).contains(MoreTimeRequestRule.ALREADY_REQUESTED_MORE_TIME_ERROR);
    }

    @Test
    public void shouldReturnValidationErrorWhenAlreadyResponded() {
        Claim claim = SampleClaim.builder()
                .withMoreTimeRequested(false)
                .withRespondedAt(LocalDateTime.now().minusDays(2))
                .build();
        List<String> errors = moreTimeRequestRule.validateMoreTimeCanBeRequested(claim);
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors).contains(MoreTimeRequestRule.ALREADY_RESPONDED_ERROR);
    }
}
