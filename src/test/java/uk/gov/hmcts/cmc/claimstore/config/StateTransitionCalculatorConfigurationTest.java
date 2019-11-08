package uk.gov.hmcts.cmc.claimstore.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.env.Environment;
import uk.gov.hmcts.cmc.claimstore.services.StateTransition;
import uk.gov.hmcts.cmc.claimstore.services.StateTransitionCalculator;
import uk.gov.hmcts.cmc.claimstore.services.WorkingDayIndicator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class StateTransitionCalculatorConfigurationTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @Mock
    private ConfigurableBeanFactory beanFactory;

    @Mock
    private Environment environment;

    private StateTransitionCalculatorConfiguration stateTransitionCalculator;

    @Before
    public void setUp() {
        stateTransitionCalculator = new StateTransitionCalculatorConfiguration(workingDayIndicator, environment);
        stateTransitionCalculator.setBeanFactory(beanFactory);
    }

    @Test
    public void shouldRegisterStateTransitionCalculatorsForConfig() {
        when(environment.getProperty(any())).thenReturn("1");

        stateTransitionCalculator.init();

        for (StateTransition stateTransition : StateTransition.values()) {
            verify(beanFactory, once()).registerSingleton(eq(String.format("%sCalculator",
                stateTransition.transitionName())), any(StateTransitionCalculator.class));
        }
    }

}
