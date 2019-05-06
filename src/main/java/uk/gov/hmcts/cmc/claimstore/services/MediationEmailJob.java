package uk.gov.hmcts.cmc.claimstore.services;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

public class MediationEmailJob implements Job {

    private String authorisation;

    private LocalDate mediationDate;

    private MediationReportService mediationReportService;

    @Autowired
    public MediationEmailJob(String authorisation, LocalDate mediationDate, MediationReportService mediationReportService) {
        this.authorisation = authorisation;
        this.mediationDate = mediationDate;
        this.mediationReportService = mediationReportService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        mediationReportService.sendMediationReport(authorisation, mediationDate);
    }
}
