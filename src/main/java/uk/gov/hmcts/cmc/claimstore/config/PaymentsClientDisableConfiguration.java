package uk.gov.hmcts.cmc.claimstore.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;

@Configuration
@ConditionalOnProperty(name = "create_claim_enabled", havingValue = "true")
public class PaymentsClientDisableConfiguration implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // Remove any auto-registered PaymentsClient when the stub is enabled.
        String[] beanNames = registry.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            if (PaymentsClient.class.getName().equals(registry.getBeanDefinition(beanName).getBeanClassName())) {
                registry.removeBeanDefinition(beanName);
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // No-op. Bean definitions are handled in postProcessBeanDefinitionRegistry.
    }
}
