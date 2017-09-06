package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class FreeMediationDecisionDateCalculator {

    private int freeMediationTimeForDecisionInDays;

    public FreeMediationDecisionDateCalculator(
        @Value("${dateCalculations.freeMediationTimeForDecisionInDays}") final int freeMediationTimeForDecisionInDays) {

        this.freeMediationTimeForDecisionInDays = freeMediationTimeForDecisionInDays;
    }

    public LocalDate calculateDecisionDate(final LocalDate responseSubmissionDate) {
        return responseSubmissionDate.plusDays(freeMediationTimeForDecisionInDays);
    }
}
