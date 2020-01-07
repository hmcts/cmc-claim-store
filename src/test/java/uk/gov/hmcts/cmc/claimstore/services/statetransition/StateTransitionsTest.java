package uk.gov.hmcts.cmc.claimstore.services.statetransition;

import org.junit.Assert;
import org.junit.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.repositories.elastic.SampleQueryConstants;

import java.time.LocalDate;
import java.util.Collections;

public class StateTransitionsTest {

    @Test
    public void stayClaimCaseEventShouldBeStayClaim() {
        Assert.assertEquals(CaseEvent.STAY_CLAIM, StateTransitions.STAY_CLAIM.getCaseEvent());
    }

    @Test
    public void stayClaimAppInsightsEventShouldBeClaimStayed() {
        Assert.assertEquals(AppInsightsEvent.CLAIM_STAYED, StateTransitions.STAY_CLAIM.getAppInsightsEvent());
    }

    @Test
    public void stayClaimCheckQuery() {
        LocalDate localDate = LocalDate.of(2019, 7, 7);
        Assert.assertEquals(SampleQueryConstants.stayableCaseQuery,
            StateTransitions.STAY_CLAIM.getQuery().apply(localDate).toString());
    }

    @Test
    public void stayClaimCheckTriggerEvents() {
        Assert.assertEquals(ImmutableSet.of(CaseEvent.DISPUTE, CaseEvent.ALREADY_PAID,  CaseEvent.FULL_ADMISSION,
            CaseEvent.PART_ADMISSION), StateTransitions.STAY_CLAIM.getTriggerEvents());
    }

    @Test
    public void stayClaimCheckIgnoredEvents() {
        ImmutableSet<CaseEvent> ignoredEvents = ImmutableSet.of(CaseEvent.LINK_LETTER_HOLDER,
            CaseEvent.SENDING_CLAIMANT_NOTIFICATION, CaseEvent.PIN_GENERATION_OPERATIONS, CaseEvent.SENDING_RPA,
            CaseEvent.SEALED_CLAIM_UPLOAD, CaseEvent.REVIEW_ORDER_UPLOAD, CaseEvent.CLAIM_ISSUE_RECEIPT_UPLOAD,
            CaseEvent.SUPPORT_UPDATE, CaseEvent.ATTACH_SCANNED_DOCS, CaseEvent.REVIEWED_PAPER_RESPONSE,
            CaseEvent.RESET_CLAIM_SUBMISSION_OPERATION_INDICATORS, CaseEvent.UPDATE_CLAIM, CaseEvent.LINK_SEALED_CLAIM);
        Assert.assertEquals(ignoredEvents, StateTransitions.STAY_CLAIM.getIgnoredEvents());
    }

    @Test
    public void waitingTransferCaseEventShouldWaitingTransfer() {
        Assert.assertEquals(CaseEvent.WAITING_TRANSFER, StateTransitions.WAITING_TRANSFER.getCaseEvent());
    }

    @Test
    public void waitingTransferAppInsightsEventShouldBeWaitingTransfer() {
        Assert.assertEquals(AppInsightsEvent.WAITING_TRANSFER, StateTransitions.WAITING_TRANSFER.getAppInsightsEvent());
    }

    @Test
    public void waitingTransferCheckQuery() {
        LocalDate localDate = LocalDate.of(2019, 7, 7);
        Assert.assertEquals(SampleQueryConstants.waitingTransferQuery,
            StateTransitions.WAITING_TRANSFER.getQuery().apply(localDate).toString());
    }

    @Test
    public void waitingTransferCheckTriggerEvents() {
        Assert.assertEquals(Collections.emptySet(), StateTransitions.WAITING_TRANSFER.getTriggerEvents());
    }

    @Test
    public void waitingTransferCheckIgnoredEvents() {
        Assert.assertEquals(Collections.emptySet(), StateTransitions.WAITING_TRANSFER.getIgnoredEvents());
    }
}
