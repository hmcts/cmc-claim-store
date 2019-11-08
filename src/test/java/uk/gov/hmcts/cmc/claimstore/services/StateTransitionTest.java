package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Assert;
import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.repositories.elastic.SampleQueryConstants;

import java.time.LocalDate;

public class StateTransitionTest {

    @Test
    public void stayClaimCaseEventShouldBeStayClaim() {
        Assert.assertEquals(CaseEvent.STAY_CLAIM, StateTransition.STAY_CLAIM.getCaseEvent());
    }

    @Test
    public void stayClaimAppInsitghtsEventShouldBeClaimStayed() {
        Assert.assertEquals(AppInsightsEvent.CLAIM_STAYED, StateTransition.STAY_CLAIM.getAppInsightsEvent());
    }

    @Test
    public void stayClaimCheckQuery() {
        LocalDate localDate = LocalDate.of(2019, 7, 7);
        Assert.assertEquals(SampleQueryConstants.stayableCaseQuery,
            StateTransition.STAY_CLAIM.getQuery().apply(localDate).toString());
    }

    @Test
    public void waitingTransferCaseEventShouldWaitingTransfer() {
        Assert.assertEquals(CaseEvent.WAITING_TRANSFER, StateTransition.WAITING_TRANSFER.getCaseEvent());
    }

    @Test
    public void waitingTransferAppInsitghtsEventShouldBeWaitingTransfer() {
        Assert.assertEquals(AppInsightsEvent.WAITING_TRANSFER, StateTransition.WAITING_TRANSFER.getAppInsightsEvent());
    }

    @Test
    public void waitingTransferCheckQuery() {
        LocalDate localDate = LocalDate.of(2019, 7, 7);
        Assert.assertEquals(SampleQueryConstants.waitingTransferQuery,
            StateTransition.WAITING_TRANSFER.getQuery().apply(localDate).toString());
    }
}
