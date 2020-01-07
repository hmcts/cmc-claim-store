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
        (responseDate -> {
            return QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("state", ClaimState.OPEN.getValue()))
                .must(QueryBuilders.rangeQuery("data.respondents.value.responseSubmittedOn").lte(responseDate))
                .mustNot(QueryBuilders.existsQuery("data.respondents.value.paidInFullDate"))
                .mustNot(QueryBuilders.existsQuery("data.respondents.value.claimantResponse.submittedOn"))
                .must(QueryBuilders.rangeQuery("data.submittedOn").gte(DateUtils.DATE_OF_5_0_0_RELEASE));
        }),
        ImmutableSet.of(CaseEvent.DISPUTE, CaseEvent.ALREADY_PAID,  CaseEvent.FULL_ADMISSION,
            CaseEvent.PART_ADMISSION),
        //TODO - Get ignorable events
        ImmutableSet.of()
    ),

    WAITING_TRANSFER(CaseEvent.WAITING_TRANSFER,
        AppInsightsEvent.WAITING_TRANSFER,
        (responseDate -> {
            return QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("state", ClaimState.ORDER_DRAWN.getValue()))
                .must(QueryBuilders.rangeQuery("data.directionOrder.createdOn").lte(responseDate));
        })
    );

    StateTransitions(CaseEvent caseEvent, AppInsightsEvent appInsightsEvent, Function<LocalDate, QueryBuilder> query) {
        this.caseEvent = caseEvent;
        this.appInsightsEvent = appInsightsEvent;
        this.query = query;
        this.triggerEvents = Collections.emptySet();
        this.ignoredEvents = Collections.emptySet();
    }

    private CaseEvent caseEvent;
    private AppInsightsEvent appInsightsEvent;
    private Function<LocalDate, QueryBuilder> query;
    private Set<CaseEvent> triggerEvents;
    private Set<CaseEvent> ignoredEvents;

}
