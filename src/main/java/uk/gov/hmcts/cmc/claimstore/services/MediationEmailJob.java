package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.Lists;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.time.LocalDate;

@Component
public class MediationEmailJob implements Job {

//    private MediationCSVService mediationCSVService;
//    private EmailService emailService;
//    private EmailProperties emailProperties;
//
//    @Autowired
//    public MediationEmailJob(MediationCSVService mediationCSVService, EmailService emailService, EmailProperties emailProperties) {
//        this.mediationCSVService = mediationCSVService;
//        this.emailService = emailService;
//        this.emailProperties = emailProperties;
//    }
//
//    public EmailData prepareMediationEmailData (byte[] mediationCSV) {
//        EmailAttachment mediationCSVAttachment =
//            EmailAttachment.csv(mediationCSV,"MediationCSV"+LocalDate.now().toString()+".csv");
//
//        return new EmailData(
//            "paul.ridings@justice.gov.uk",
//            "MediationCSV "+ LocalDate.now().toString(),
//            "OCMC Mediation" + LocalDate.now().toString(),
//            Lists.newArrayList(mediationCSVAttachment)
//        );
//    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

    }
}
