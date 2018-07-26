package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIM;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.LETTER_HOLDER_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.USER_ID;

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
    public void shouldSaveClaimInRepository() {
        //given
        ClaimData input = SampleClaimData.validDefaults();
        when(claimService.saveClaim(eq(USER_ID), eq(input), eq(AUTHORISATION), )).thenReturn(CLAIM);

        //when
        Claim output = claimController.save(input, USER_ID, AUTHORISATION);

        //then
        assertThat(output).isEqualTo(CLAIM);
    }

    @Test
    public void shouldReturnClaimFromRepositoryForClaimantId() {
        //given
        when(claimService.getClaimBySubmitterId(eq(USER_ID), eq(AUTHORISATION)))
            .thenReturn(Collections.singletonList(CLAIM));

        //when
        List<Claim> output = claimController.getBySubmitterId(USER_ID, AUTHORISATION);

        //then
        assertThat(output.get(0)).isEqualTo(CLAIM);
    }

    @Test
    public void shouldReturnClaimFromRepositoryForLetterHolderId() {
        //given
        when(claimService.getClaimByLetterHolderId(eq(LETTER_HOLDER_ID), any())).thenReturn(CLAIM);

        //when
        Claim output = claimController.getByLetterHolderId(LETTER_HOLDER_ID, AUTHORISATION);

        //then
        assertThat(output).isEqualTo(CLAIM);
    }

}
