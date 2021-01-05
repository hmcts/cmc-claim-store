package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.CaseEventService;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseEventControllerTest {

    private static final String AUTHORISATION = "Bearer: aaa";

    private CaseEventController caseEventController;

    @Mock
    private CaseEventService caseEventService;

    private List<CaseEvent> caseEventList = new ArrayList<>();

    @Before
    public void setup() {
        caseEventController = new CaseEventController(caseEventService);
        caseEventList.add(CaseEvent.CLOSE_AWAITING_RESPONSE_HWF);
        caseEventList.add(CaseEvent.MISC_HWF);
        caseEventList.add(CaseEvent.HWF_PART_REMISSION_GRANTED);
    }

    @Test
    public void shouldFindEventsForCasesController() {
        //given
        ClaimData input = SampleClaimData.validDefaults();
        when(caseEventService.findEventsForCase(AUTHORISATION, "1")).thenReturn(caseEventList);

        List<CaseEvent> caseEventListOutput = caseEventController.findEventsForCase("1", AUTHORISATION);

        //then
        assertThat(caseEventListOutput).isEqualTo(caseEventList);
    }

}
