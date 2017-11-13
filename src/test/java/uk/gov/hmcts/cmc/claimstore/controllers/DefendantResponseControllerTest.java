package uk.gov.hmcts.cmc.claimstore.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.ResponseData;
import uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleResponseData;
import uk.gov.hmcts.cmc.claimstore.services.DefendantResponseService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleClaim.CLAIM_ID;
import static uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleClaim.DEFENDANT_ID;

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
    public void shouldSaveResponseInRepository() throws JsonProcessingException {
        //given
        final ResponseData input = SampleResponseData.builder()
            .build();

        Claim sampleClaim = SampleClaim.getWithDefaultResponse();
        when(responseService.save(eq(CLAIM_ID), eq(DEFENDANT_ID), eq(input), eq(AUTHORISATION)))
            .thenReturn(sampleClaim);

        //when
        Claim output = defendantResponseController.save(input, DEFENDANT_ID, CLAIM_ID, AUTHORISATION);

        //then
        assertThat(output).isEqualTo(sampleClaim);
    }
}
