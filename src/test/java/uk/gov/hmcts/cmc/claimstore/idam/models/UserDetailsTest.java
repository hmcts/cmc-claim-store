package uk.gov.hmcts.cmc.claimstore.idam.models;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;

import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.SUBMITTER_FORENAME;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.SUBMITTER_SURNAME;

public class UserDetailsTest {
    @Test
    public void shouldReturnFullNameWhenBothArePresent() throws Exception {
        //given
        final UserDetails userDetails = SampleUserDetails.builder().build();

        //when
        final String fullName = userDetails.getFullName();

        //then
        Assertions.assertThat(fullName).isEqualTo(SUBMITTER_FORENAME + " " + SUBMITTER_SURNAME);
    }

    @Test
    public void shouldReturnForNameWhenSurnameIsMissing() throws Exception {
        //given
        final UserDetails userDetails = SampleUserDetails.builder().withSurname(null).build();

        //when
        final String fullName = userDetails.getFullName();

        //then
        Assertions.assertThat(fullName).isEqualTo(SUBMITTER_FORENAME);
    }
}
