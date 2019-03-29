package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.time.LocalDate;

@Service
public class MediationCSVService {

    private EmailService emailService;
    private MediationCSVGenerator mediationCSVGenerator;

    @Autowired
    public MediationCSVService(
        EmailService emailService,
        MediationCSVGenerator mediationCSVGenerator
    ) {
        this.emailService = emailService;
        this.mediationCSVGenerator = mediationCSVGenerator;
    }

    public void sendMediationCSV(String authorisation, LocalDate mediationDate) {
        emailService.sendEmail("kiran.varma@hmcts.net",
            prepareMediationEmailData(mediationCSVGenerator.createMediationCSV(authorisation, mediationDate)));
    }

    private EmailData prepareMediationEmailData (String mediationCSV) {
        EmailAttachment mediationCSVAttachment =
            EmailAttachment.csv(mediationCSV.getBytes(),"MediationCSV"+LocalDate.now().toString()+".csv");

        return new EmailData(
            "kiran.varma@hmcts.net",
            "MediationCSV "+ LocalDate.now().toString(),
            "OCMC Mediation" + LocalDate.now().toString(),
            Lists.newArrayList(mediationCSVAttachment)
        );
    }
}
