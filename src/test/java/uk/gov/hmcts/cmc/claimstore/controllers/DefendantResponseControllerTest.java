package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.DefendantResponseService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.EXTERNAL_ID;

@RunWith(MockitoJUnitRunner.class)
public class DefendantResponseControllerTest {
    private static final String AUTHORISATION = "Bearer: aaa";

    private DefendantResponseController defendantResponseController;

    @Mock
    private DefendantResponseService responseService;

    @Before
    public void setup() {
        defendantResponseController = new DefendantResponseController(responseService);
    }

    @Test
    public void shouldSaveResponseInRepository() {
        //given
        Response input = SampleResponse.FullDefence.builder()
            .build();

        Claim sampleClaim = SampleClaim.getWithDefaultResponse();
        when(responseService.save(eq(EXTERNAL_ID), eq(DEFENDANT_ID), eq(input), eq(AUTHORISATION)))
            .thenReturn(sampleClaim);

        //when
        Claim output = defendantResponseController.save(input, DEFENDANT_ID, EXTERNAL_ID, AUTHORISATION);

        //then
        assertThat(output).isEqualTo(sampleClaim);
    }
}
