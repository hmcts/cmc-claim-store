package uk.gov.hmcts.cmc.claimstore.services.statetransition;

import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.utils.DateUtils;
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
        (responseDate -> QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("state", ClaimState.OPEN.getValue()))
                .must(QueryBuilders.rangeQuery("data.respondents.value.responseSubmittedOn").lte(responseDate))
                .mustNot(QueryBuilders.existsQuery("data.respondents.value.paidInFullDate"))
                .mustNot(QueryBuilders.existsQuery("data.respondents.value.claimantResponse.submittedOn"))
                .must(QueryBuilders.rangeQuery("data.submittedOn").gte(DateUtils.DATE_OF_5_0_0_RELEASE))
        ),
        ImmutableSet.of(CaseEvent.DISPUTE, CaseEvent.ALREADY_PAID,  CaseEvent.FULL_ADMISSION,
            CaseEvent.PART_ADMISSION),
        ImmutableSet.of(CaseEvent.LINK_LETTER_HOLDER, CaseEvent.SENDING_CLAIMANT_NOTIFICATION,
            CaseEvent.PIN_GENERATION_OPERATIONS, CaseEvent.SENDING_RPA, CaseEvent.SEALED_CLAIM_UPLOAD,
            CaseEvent.REVIEW_ORDER_UPLOAD, CaseEvent.CLAIM_ISSUE_RECEIPT_UPLOAD, CaseEvent.SUPPORT_UPDATE,
            CaseEvent.ATTACH_SCANNED_DOCS, CaseEvent.REVIEWED_PAPER_RESPONSE,
            CaseEvent.RESET_CLAIM_SUBMISSION_OPERATION_INDICATORS, CaseEvent.UPDATE_CLAIM, CaseEvent.LINK_SEALED_CLAIM)
    ),

    WAITING_TRANSFER(CaseEvent.WAITING_TRANSFER,
        AppInsightsEvent.WAITING_TRANSFER,
        (responseDate -> QueryBuilders.boolQuery()
                // state must be lower cased regardless of case format in CCD database
                .must(QueryBuilders.termQuery("state", ClaimState.ORDER_DRAWN.getValue().toLowerCase()))
                .must(QueryBuilders.rangeQuery("data.directionOrder.createdOn").lte(responseDate))
        )
    );

    private CaseEvent caseEvent;
    private AppInsightsEvent appInsightsEvent;
    private Function<LocalDate, QueryBuilder> query;
    private Set<CaseEvent> triggerEvents;
    private Set<CaseEvent> ignoredEvents;

    StateTransitions(CaseEvent caseEvent, AppInsightsEvent appInsightsEvent, Function<LocalDate, QueryBuilder> query) {
        this.caseEvent = caseEvent;
        this.appInsightsEvent = appInsightsEvent;
        this.query = query;
        this.triggerEvents = Collections.emptySet();
        this.ignoredEvents = Collections.emptySet();
    }
}
