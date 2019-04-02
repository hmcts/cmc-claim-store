package uk.gov.hmcts.cmc.claimstore.services;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

public class MediationEmailJob implements Job {

    private String authorisation;

    private LocalDate mediationDate;

    private MediationCSVService mediationCSVService;

    @Autowired
    public MediationEmailJob(String authorisation, LocalDate mediationDate, MediationCSVService mediationCSVService) {
        this.authorisation = authorisation;
        this.mediationDate = mediationDate;
        this.mediationCSVService = mediationCSVService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        mediationCSVService.sendMediationCSV(authorisation, mediationDate);
    }
}
