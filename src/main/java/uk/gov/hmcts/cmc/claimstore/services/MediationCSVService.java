package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MediationCSVService {

    private EmailService emailService;
    private MediationCSVGenerator mediationCSVGenerator;
    private final UserService userService;

    @Autowired
    public MediationCSVService(
        EmailService emailService,
        MediationCSVGenerator mediationCSVGenerator,
        UserService userService
    ) {
        this.emailService = emailService;
        this.mediationCSVGenerator = mediationCSVGenerator;
        this.userService = userService;
    }

    public void sendMediationCSV(String authorisation, LocalDate mediationDate) {
        emailService.sendEmail("andrew.walker@hmcts.net",
            prepareMediationEmailData(mediationCSVGenerator.createMediationCSV(authorisation, mediationDate)));
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void automatedMediationCSV() {
        emailService.sendEmail(
            "andrew.walker@hmcts.net",
            prepareMediationEmailData(
                mediationCSVGenerator.createMediationCSV(
                    userService.authenticateAnonymousCaseWorker().getAuthorisation(),
                    LocalDate.now().minusDays(1))
            ));
    }

    private EmailData prepareMediationEmailData(String mediationCSV) {
        EmailAttachment mediationCSVAttachment =
            EmailAttachment.csv(mediationCSV.getBytes(), "MediationCSV" + LocalDate.now().toString() + ".csv");

        return new EmailData(
            "andrew.walker@hmcts.net",
            "MediationCSV " + LocalDate.now().toString(),
            "OCMC mediation" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm dd/mm/yyyy")),
            Lists.newArrayList(mediationCSVAttachment)
        );
    }
}
