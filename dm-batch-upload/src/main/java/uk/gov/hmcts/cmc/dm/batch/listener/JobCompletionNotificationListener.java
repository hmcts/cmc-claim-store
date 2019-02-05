package uk.gov.hmcts.cmc.dm.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.dm.batch.enums.JobName;

import static uk.gov.hmcts.cmc.dm.batch.db.Query.SEALED_CLAIM_COUNT;

@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("*******************************************");
            log.info("{} completed", jobExecution.getJobInstance().getJobName());
            log.info("Total execution time  in minutes {}",
                (jobExecution.getEndTime().getTime() - jobExecution.getStartTime().getTime()) / (60 * 60 * 1000));
            log.info("*******************************************");

        }
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("*******************************************");
        log.info("{} Starting",
            jobExecution.getJobInstance().getJobName());

        switch (JobName.valueOf(jobExecution.getJobInstance().getJobName())) {
            case UPLOAD_SEALED_CLAIM:
                log.info("Total number of records to process {} ", getCount(SEALED_CLAIM_COUNT));
                break;
            default:
                log.info("Unrecognised job found. JobName: {}", jobExecution.getJobInstance().getJobName());
        }
        log.info("*******************************************");
    }

    private Long getCount(String query) {
        return jdbcTemplate.queryForObject(query, Long.class);
    }
}
