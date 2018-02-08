package uk.gov.hmcts.cmc.claimstore.idam.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.SUBMITTER_FORENAME;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.SUBMITTER_SURNAME;

public class UserDetailsTest {
    @Test
    public void shouldReturnFullNameWhenBothArePresent() throws Exception {
        //given
        UserDetails userDetails = SampleUserDetails.builder().build();

        //when
        String fullName = userDetails.getFullName();

        //then
        assertThat(fullName).isEqualTo(SUBMITTER_FORENAME + " " + SUBMITTER_SURNAME);
    }

    @Test
    public void shouldReturnForNameWhenSurnameIsMissing() throws Exception {
        //given
        UserDetails userDetails = SampleUserDetails.builder().withSurname(null).build();

        //when
        String fullName = userDetails.getFullName();

        //then
        assertThat(fullName).isEqualTo(SUBMITTER_FORENAME);
    }

    @Test
    public void shouldReturnTrueWhenSolicitorRolePresent() {

    }

    public void shouldReturnFalseWhenSolicitorRoleNotPresent() {

    }

    public void shouldReturnTrueWhenMultipleRolesPresent() {

    }
}
