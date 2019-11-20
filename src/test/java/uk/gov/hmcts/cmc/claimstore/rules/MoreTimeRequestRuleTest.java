package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeAlreadyRequestedException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils.containsOnly;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;

@RunWith(MockitoJUnitRunner.class)
public class MoreTimeRequestRuleTest {

    @Spy
    private ClaimDeadlineService claimDeadlineService = new ClaimDeadlineService();

    private MoreTimeRequestRule moreTimeRequestRule;

    public static final String ALREADY_REQUESTED_MORE_TIME_ERROR = "The defendant already asked for more time "
            + "and their request was processed";
    public static final String PAST_DEADLINE_ERROR = "The defendant has missed the deadline for requesting more time";
    public static final String ALREADY_RESPONDED_ERROR = "You can’t process this paper request for more time "
            + "because the defendant already responded to the claim digitally.";

    @Before
    public void beforeEachTest() {
        moreTimeRequestRule = new MoreTimeRequestRule(claimDeadlineService);
    }

    @Test
    public void noExceptionThrownWhenMoreTimeRequestedFirstTimeAndDeadlineHasNotPassed() {
        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().plusDays(2))
            .withMoreTimeRequested(false)
            .build();
        assertThatCode(() -> moreTimeRequestRule.assertMoreTimeCanBeRequested(claim)).doesNotThrowAnyException();
    }

    @Test
    public void noExceptionThrownWhenMoreTimeRequestedFirstTimeAndDeadlineHasPassed() {
        Claim claim = SampleClaim.builder()
                .withResponseDeadline(LocalDate.now().minusDays(2))
                .withMoreTimeRequested(false)
                .build();
        assertThatCode(() -> moreTimeRequestRule.assertMoreTimeCanBeRequested(claim)).doesNotThrowAnyException();
    }

    @Test(expected = MoreTimeAlreadyRequestedException.class)
    public void shouldThrowExceptionWhenMoreTimeWasAlreadyRequested() {
        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().plusDays(2))
            .withMoreTimeRequested(true)
            .build();
        moreTimeRequestRule.assertMoreTimeCanBeRequested(claim);
    }

//    public List<String> validateMoreTimeCanBeRequested(Claim claim, LocalDate newDeadline) {
//        Objects.requireNonNull(claim, "claim object can not be null");
//
//        List<String> validationErrors = new ArrayList<>();
//
//        if (claim.getRespondedAt() != null) {
//            validationErrors.add(ALREADY_RESPONDED_ERROR);
//        }
//
//        if (claim.isMoreTimeRequested()) {
//            validationErrors.add(ALREADY_REQUESTED_MORE_TIME_ERROR);
//        }
//
//        if (claimDeadlineService.isPastDeadline(nowInLocalZone(), newDeadline)) {
//            validationErrors.add(PAST_DEADLINE_ERROR);
//        }
//
//        return validationErrors;
//    }

    @Test(expected = MoreTimeAlreadyRequestedException.class)
    public void shouldReturnValidationErrorWhenAlreadyResponded() {
        LocalDate deadlineDay = LocalDate.now().plusDays(2);
        Claim claim = SampleClaim.builder()
                .withResponseDeadline(deadlineDay)
                .withMoreTimeRequested(true)
                .build();
        assertThat(moreTimeRequestRule.validateMoreTimeCanBeRequested(claim, deadlineDay)).contains(ALREADY_REQUESTED_MORE_TIME_ERROR);
    }
}
