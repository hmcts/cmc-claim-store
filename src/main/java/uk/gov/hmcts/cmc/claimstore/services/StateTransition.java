package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.base.CaseFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.domain.models.ClaimState;

import java.time.LocalDate;
import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum StateTransition {

    STAY_CLAIM(CaseEvent.STAY_CLAIM,
        AppInsightsEvent.CLAIM_STAYED,
        (responseDate -> {
            return QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("state", ClaimState.OPEN.getValue()))
                .must(QueryBuilders.rangeQuery("data.respondents.value.responseSubmittedOn").lte(responseDate))
                .must(QueryBuilders.rangeQuery("data.submittedOn")
                    .gte(ScheduledStateTransitionService.DATE_OF_5_POINT_0_RELEASE));
        })
    ),

    WAITING_TRANSFER(CaseEvent.WAITING_TRANSFER,
        AppInsightsEvent.WAITING_TRANSFER,
        (responseDate -> {
            return QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("state", ClaimState.ORDER_DRAWN.getValue()))
                .must(QueryBuilders.rangeQuery("data.directionOrder.createdOn").lte(responseDate));
        })
    );

    private CaseEvent caseEvent;
    private AppInsightsEvent appInsightsEvent;
    private Function<LocalDate, QueryBuilder> query;

    public String transitionName() {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
    }
}
