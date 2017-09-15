package uk.gov.hmcts.cmc.claimstore.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleDefendantResponse;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleResponseData;
import uk.gov.hmcts.cmc.claimstore.models.ResponseData;
import uk.gov.hmcts.cmc.claimstore.services.DefendantResponseService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleDefendantResponse.CLAIM_ID;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleDefendantResponse.DEFENDANT_ID;

@RunWith(MockitoJUnitRunner.class)
public class DefendantResponseControllerTest {
    private static final DefendantResponse DEFENDANT_RESPONSE = SampleDefendantResponse.getDefault();
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

        when(responseService.save(eq(CLAIM_ID), eq(DEFENDANT_ID), eq(input), eq(AUTHORISATION)))
            .thenReturn(DEFENDANT_RESPONSE);

        //when
        DefendantResponse output = defendantResponseController.save(input, DEFENDANT_ID, CLAIM_ID, AUTHORISATION);

        //then
        assertThat(output).isEqualTo(DEFENDANT_RESPONSE);
    }

    @Test
    public void shouldReturnDefendantResponseFromRepositoryForDefendantId() throws JsonProcessingException {
        //given
        when(responseService.getByDefendantId(eq(DEFENDANT_ID)))
            .thenReturn(Collections.singletonList(DEFENDANT_RESPONSE));

        //when
        List<DefendantResponse> output = defendantResponseController.getByDefendantId(DEFENDANT_ID);

        //then
        assertThat(output).isEqualTo(Collections.singletonList(DEFENDANT_RESPONSE));
    }
}
