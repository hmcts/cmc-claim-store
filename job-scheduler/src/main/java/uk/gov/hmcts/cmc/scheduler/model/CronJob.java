package uk.gov.hmcts.cmc.scheduler.model;

import org.quartz.Job;

public interface CronJob extends Job {

    String getCronExpression();
}
