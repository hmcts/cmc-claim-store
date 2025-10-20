package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;

import static uk.gov.hmcts.cmc.claimstore.utils.DayAssert.assertThat;

public class FreeMediationDecisionDateCalculatorTest {

    private static final int DAYS_FOR_DECISION = 28;

    private FreeMediationDecisionDateCalculator calculator;

    @Before
    public void setUp() {
        calculator = new FreeMediationDecisionDateCalculator(DAYS_FOR_DECISION);
    }

    @Test
    public void calculatesValidDate() {

        LocalDate responseSubmissionDate = LocalDate.now();
        LocalDate decisionDeadline = calculator.calculateDecisionDate(responseSubmissionDate);

        assertThat(decisionDeadline).isNumberOfDaysSince(DAYS_FOR_DECISION, responseSubmissionDate);
    }
}
