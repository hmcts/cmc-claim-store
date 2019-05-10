package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
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
    private AppInsights appInsights;

    private final String emailToAddress;
    private final String emailFromAddress;

    @Autowired
    public MediationReportService(
        EmailService emailService,
        MediationCSVGenerator mediationCSVGenerator,
        UserService userService,
        AppInsights appInsights,
        @Value("${milo.recipient}") String emailToAddress,
        @Value("${milo.sender}") String emailFromAddress
    ) {
        this.emailService = emailService;
        this.mediationCSVGenerator = mediationCSVGenerator;
        this.userService = userService;
        this.appInsights = appInsights;
        this.emailToAddress = emailToAddress;
        this.emailFromAddress = emailFromAddress;
    }

    public void sendMediationReport(String authorisation, LocalDate mediationDate) {
        try {
            String csvData = mediationCSVGenerator.createMediationCSV(
                authorisation,
                mediationDate
            );

            emailService.sendEmail(emailFromAddress, prepareMediationEmailData(csvData));
        } catch (RuntimeException e) {
            reportMediationException(e, mediationDate);
        }
    }

    @Scheduled(cron = "#{'${milo.schedule}' ?: '-'}")
    public void automatedMediationReport() {
        sendMediationReport(
            userService.authenticateAnonymousCaseWorker().getAuthorisation(),
            LocalDate.now().minusDays(1)
        );
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

    private void reportMediationException(RuntimeException e, LocalDate reportDate) {
        appInsights.trackEvent(
            AppInsightsEvent.MEDIATION_REPORT_FAILURE,
            "MILO report " + reportDate,
            e.getMessage()
        );
        throw e;
    }
}
