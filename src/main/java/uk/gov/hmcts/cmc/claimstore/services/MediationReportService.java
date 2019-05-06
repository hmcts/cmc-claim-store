package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MediationReportService {

    private EmailService emailService;
    private MediationCSVGenerator mediationCSVGenerator;
    private UserService userService;

    private final String emailToAddress;
    private final String emailFromAddress;

    @Autowired
    public MediationReportService(
        EmailService emailService,
        MediationCSVGenerator mediationCSVGenerator,
        UserService userService,
        @Value("${milo.recipient}") String emailToAddress,
        @Value("${milo.sender}") String emailFromAddress
    ) {
        this.emailService = emailService;
        this.mediationCSVGenerator = mediationCSVGenerator;
        this.userService = userService;
        this.emailToAddress = emailToAddress;
        this.emailFromAddress = emailFromAddress;
    }

    public void sendMediationReport(String authorisation, LocalDate mediationDate) {
        emailService.sendEmail(emailFromAddress,
            prepareMediationEmailData(mediationCSVGenerator.createMediationCSV(authorisation, mediationDate)));
    }

    @Scheduled(cron = "#{'${milo.schedule}' ?: '-'}")
    public void automatedMediationReport() {
        final String csvData = mediationCSVGenerator.createMediationCSV(
            userService.authenticateAnonymousCaseWorker().getAuthorisation(),
            LocalDate.now().minusDays(1)
        );
        emailService.sendEmail(emailFromAddress, prepareMediationEmailData(csvData));
    }

    private EmailData prepareMediationEmailData(String mediationCSV) {
        String fileName = "MediationCSV." + LocalDate.now().toString() + ".csv";
        EmailAttachment mediationCSVAttachment = EmailAttachment.csv(mediationCSV.getBytes(), fileName);

        return new EmailData(
            emailToAddress,
            "MediationCSV " + LocalDate.now().toString(),
            "OCMC mediation" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm dd/mm/yyyy")),
            Lists.newArrayList(mediationCSVAttachment)
        );
    }
}
