package uk.gov.hmcts.cmc.claimstore.config;

import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import uk.gov.hmcts.cmc.claimstore.services.StateTransition;
import uk.gov.hmcts.cmc.claimstore.services.StateTransitionCalculator;
import uk.gov.hmcts.cmc.claimstore.services.WorkingDayIndicator;

import javax.annotation.PostConstruct;

@Configuration
@Setter
public class StateTransitionCalculatorConfiguration implements BeanFactoryAware {

    private final WorkingDayIndicator workingDayIndicator;

    private final Environment environment;

    private BeanFactory beanFactory;

    public StateTransitionCalculatorConfiguration(WorkingDayIndicator workingDayIndicator, Environment environment) {
        this.workingDayIndicator = workingDayIndicator;
        this.environment = environment;
    }

    @PostConstruct
    public void init() throws BeansException {

        for (StateTransition stateTransition : StateTransition.values()) {
            String numberOfDaysPropertyKey = String.format("dateCalculations.%sDeadlineInDays",
                stateTransition.transitionName());

            String numberOfDaysProperty = environment.getProperty(numberOfDaysPropertyKey);

            if (StringUtils.isBlank(numberOfDaysProperty)) {
                throw new IllegalArgumentException(String.format("Missing property %s", numberOfDaysPropertyKey));
            }

            ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
            configurableBeanFactory.registerSingleton(String.format("%sCalculator", stateTransition.transitionName()),
                new StateTransitionCalculator(workingDayIndicator, Integer.parseInt(numberOfDaysProperty)));
        }
    }
}
