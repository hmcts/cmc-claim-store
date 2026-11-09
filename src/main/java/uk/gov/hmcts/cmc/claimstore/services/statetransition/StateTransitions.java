package uk.gov.hmcts.cmc.claimstore.services.statetransition;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.repositories.elastic.Queries;
import uk.gov.hmcts.cmc.claimstore.repositories.elastic.SearchClause;
import uk.gov.hmcts.cmc.domain.models.ClaimState;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum StateTransitions implements StateTransition {

    STAY_CLAIM(CaseEvent.STAY_CLAIM,
        AppInsightsEvent.CLAIM_STAYED,
        (responseDate -> Queries.boolQuery()
                .must(Queries.termQuery("state", ClaimState.OPEN.getValue()))
                .must(Queries.rangeQuery("data.respondents.value.responseSubmittedOn").lte(responseDate))
                .mustNot(Queries.existsQuery("data.respondents.value.paidInFullDate"))
                .mustNot(Queries.existsQuery("data.respondents.value.claimantResponse.submittedOn"))
                .must(Queries.rangeQuery("data.submittedOn")
                    .gte(calculateClaimSubmittedOnDate(responseDate, Constants.CLAIM_CREATED_BEFORE_DAYS_COUNT))
                    .lte(responseDate))
        ),
        Set.of(CaseEvent.DISPUTE, CaseEvent.ALREADY_PAID,  CaseEvent.FULL_ADMISSION,
            CaseEvent.PART_ADMISSION, CaseEvent.PAPER_RESPONSE_FULL_DEFENCE),
        Set.of(CaseEvent.LINK_LETTER_HOLDER, CaseEvent.DEFENDANT_RESPONSE_UPLOAD,
            CaseEvent.SENDING_CLAIMANT_NOTIFICATION, CaseEvent.PIN_GENERATION_OPERATIONS, CaseEvent.SENDING_RPA,
            CaseEvent.SEALED_CLAIM_UPLOAD, CaseEvent.REVIEW_ORDER_UPLOAD, CaseEvent.CLAIM_ISSUE_RECEIPT_UPLOAD,
            CaseEvent.SUPPORT_UPDATE, CaseEvent.ATTACH_SCANNED_DOCS, CaseEvent.REVIEWED_PAPER_RESPONSE,
            CaseEvent.RESET_CLAIM_SUBMISSION_OPERATION_INDICATORS, CaseEvent.UPDATE_CLAIM, CaseEvent.LINK_SEALED_CLAIM,
            CaseEvent.CLAIM_NOTES, CaseEvent.PAPER_HAND_OFF)
    ),

    WAITING_TRANSFER(CaseEvent.WAITING_TRANSFER,
        AppInsightsEvent.WAITING_TRANSFER,
        (responseDate -> Queries.boolQuery()
                // state must be lower cased regardless of case format in CCD database
                .must(Queries.termQuery("state", ClaimState.ORDER_DRAWN.getValue().toLowerCase()))
                .must(Queries.rangeQuery("data.directionOrder.createdOn").lte(responseDate))
        )
    );

    private CaseEvent caseEvent;
    private AppInsightsEvent appInsightsEvent;
    private Function<LocalDate, SearchClause> query;
    private Set<CaseEvent> triggerEvents;
    private Set<CaseEvent> ignoredEvents;

    StateTransitions(CaseEvent caseEvent, AppInsightsEvent appInsightsEvent, Function<LocalDate, SearchClause> query) {
        this.caseEvent = caseEvent;
        this.appInsightsEvent = appInsightsEvent;
        this.query = query;
        this.triggerEvents = Collections.emptySet();
        this.ignoredEvents = Collections.emptySet();
    }

    public static LocalDate calculateClaimSubmittedOnDate(LocalDate runDateTime, int numberOfDays) {
        return runDateTime.minusDays(numberOfDays);
    }

    private static class Constants {
        public static final int CLAIM_CREATED_BEFORE_DAYS_COUNT = 60;
    }
}
