package uk.gov.hmcts.cmc.scheduler.config;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.cmc.scheduler.model.CronJob;
import uk.gov.hmcts.cmc.scheduler.model.JobData;
import uk.gov.hmcts.cmc.scheduler.services.JobService;

import javax.annotation.PostConstruct;
import java.util.HashMap;

@Configuration
@AllArgsConstructor
@Setter
public class CronJobConfiguration implements BeanFactoryAware {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JobService jobService;
    private BeanFactory beanFactory;

    @Autowired
    public CronJobConfiguration(JobService jobService) {
        this.jobService = jobService;
    }

    @PostConstruct
    public void init() throws BeansException {
        ListableBeanFactory listableBeanFactory = (ListableBeanFactory) beanFactory;
        for (CronJob cronJob : listableBeanFactory.getBeansOfType(CronJob.class).values()) {

            JobData jobData = JobData.builder()
                .id(cronJob.getClass().getName())
                .jobClass(cronJob.getClass())
                .data(new HashMap<>())
                .build();

            jobService.scheduleJob(jobData, cronJob.getCronExpression() );
        }
    }
}
