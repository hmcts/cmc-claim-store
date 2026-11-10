package uk.gov.hmcts.cmc.claimstore.services.statetransition;

import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.repositories.elastic.SearchClause;

import java.time.LocalDate;
import java.util.Set;
import java.util.function.Function;

public interface StateTransition {

    CaseEvent getCaseEvent();

    AppInsightsEvent getAppInsightsEvent();

    Function<LocalDate, SearchClause> getQuery();

    Set<CaseEvent> getTriggerEvents();

    Set<CaseEvent> getIgnoredEvents();

    String name();
}
