package uk.gov.hmcts.cmc.claimstore.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.LETTER_HOLDER_ID;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.USER_ID;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIM;

@RunWith(MockitoJUnitRunner.class)
public class ClaimControllerTest {

    private static final String AUTHORISATION = "Bearer: aaa";

    private ClaimController claimController;

    @Mock
    private ClaimService claimService;

    @Before
    public void setup() {
        claimController = new ClaimController(claimService);
    }

    @Test
    public void shouldSaveClaimInRepository() throws JsonProcessingException {
        //given
        final ClaimData input = SampleClaimData.validDefaults();
        when(claimService.saveClaim(eq(USER_ID), eq(input), eq(AUTHORISATION))).thenReturn(CLAIM);

        //when
        Claim output = claimController.save(input, USER_ID, AUTHORISATION);

        //then
        assertThat(output).isEqualTo(CLAIM);
    }

    @Test
    public void shouldReturnClaimFromRepositoryForClaimantId() throws JsonProcessingException {
        //given
        when(claimService.getClaimBySubmitterId(eq(USER_ID))).thenReturn(Collections.singletonList(CLAIM));

        //when
        final List<Claim> output = claimController.getBySubmitterId(USER_ID);

        //then
        assertThat(output.get(0)).isEqualTo(CLAIM);
    }

    @Test
    public void shouldReturnClaimFromRepositoryForLetterHolderId() throws JsonProcessingException {
        //given
        when(claimService.getClaimByLetterHolderId(eq(LETTER_HOLDER_ID))).thenReturn(CLAIM);

        //when
        Claim output = claimController.getByLetterHolderId(LETTER_HOLDER_ID);

        //then
        assertThat(output).isEqualTo(CLAIM);
    }
}
