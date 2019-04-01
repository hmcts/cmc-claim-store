package uk.gov.hmcts.cmc.claimstore.services;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDate;

public class MediationEmailJob implements Job {

    private MediationCSVService mediationCSVService;
    private String authorisation;
    private LocalDate mediationDate;

    public MediationEmailJob(
        MediationCSVService mediationCSVService,
        String authorisation,
        LocalDate mediationDate
    ) {
        this.mediationCSVService = mediationCSVService;
        this.authorisation = authorisation;
        this.mediationDate = mediationDate;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        mediationCSVService.sendMediationCSV(authorisation, mediationDate);
    }
}
