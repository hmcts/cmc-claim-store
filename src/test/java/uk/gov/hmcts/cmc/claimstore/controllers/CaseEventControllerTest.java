package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.CaseEventService;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.ioc.CreatePaymentResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleReviewOrder;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIM;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.ADMISSIONS;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.*;

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
    public void shouldSaveClaimInRepository() {
        //given
        ClaimData input = SampleClaimData.validDefaults();
        when(caseEventService.findEventsForCase(AUTHORISATION, "1")).thenReturn(caseEventList);


        List<CaseEvent> caseEventListOutput = caseEventController.findEventsForCase("1", AUTHORISATION);

        //then
        assertThat(caseEventListOutput).isEqualTo(caseEventList);
    }

}
