package uk.gov.hmcts.cmc.claimstore.services;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MediationEmailJob implements Job {

    private MediationCSVService mediationCSVService;

    @Autowired
    public MediationEmailJob(MediationCSVService mediationCSVService) {
        this.mediationCSVService = mediationCSVService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDetail jobDetail = context.getJobDetail();
        mediationCSVService.sendMediationCSV(jobDetail);
        logger.debug("Completed job work for id {}", jobDetail.getKey().getName());
    }
}
