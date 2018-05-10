package uk.gov.hmcts.cmc.scheduler.config;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * Adds autowiring support to Quartz jobs.
 *
 * @see "https://gist.github.com/jelies/5085593"
 */
public class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {

    private AutowireCapableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.beanFactory = applicationContext.getAutowireCapableBeanFactory();
    }

    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle) {
        Object job = this.beanFactory.autowire(
            bundle.getJobDetail().getJobClass(),
            AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR,
            true
        );

        this.beanFactory.autowireBean(job);

        return job;
    }
}
