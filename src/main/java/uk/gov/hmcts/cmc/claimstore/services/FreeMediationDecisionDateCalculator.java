package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class FreeMediationDecisionDateCalculator {

    private final int freeMediationTimeForDecisionInDays;

    public FreeMediationDecisionDateCalculator(
        @Value("${dateCalculations.freeMediationTimeForDecisionInDays}") int freeMediationTimeForDecisionInDays) {

        this.freeMediationTimeForDecisionInDays = freeMediationTimeForDecisionInDays;
    }

    public LocalDate calculateDecisionDate(LocalDate responseSubmissionDate) {
        return responseSubmissionDate.plusDays(freeMediationTimeForDecisionInDays);
    }
}
