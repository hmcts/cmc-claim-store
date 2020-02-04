package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.MediationCSVGenerationException;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

@Service
public class MediationReportService {

    private final EmailService emailService;
    private final CaseSearchApi caseSearchApi;
    private final UserService userService;
    private final AppInsights appInsights;

    private final String emailToAddress;
    private final String emailFromAddress;

    @Autowired
    public MediationReportService(
        EmailService emailService,
        CaseSearchApi caseSearchApi,
        UserService userService,
        AppInsights appInsights,
        @Value("${milo.recipient}") String emailToAddress,
        @Value("${milo.sender}") String emailFromAddress
    ) {
        this.emailService = emailService;
        this.caseSearchApi = caseSearchApi;
        this.userService = userService;
        this.appInsights = appInsights;
        this.emailToAddress = emailToAddress;
        this.emailFromAddress = emailFromAddress;
    }

    public void sendMediationReport(String authorisation, LocalDate mediationDate) {
        try {
            MediationCSVGenerator generator = new MediationCSVGenerator(caseSearchApi, mediationDate, authorisation);
            generator.createMediationCSV();
            String csvData = generator.getCsvData();

            emailService.sendEmail(emailFromAddress, prepareMediationEmailData(csvData));

            Map<String, String> problematicRecords = generator.getProblematicRecords();
            if (!problematicRecords.isEmpty()) {
                reportMediationExceptions(mediationDate, problematicRecords);
            }
        } catch (MediationCSVGenerationException e) {
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
            Collections.singletonList(mediationCSVAttachment)
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

    private void reportMediationExceptions(LocalDate reportDate, Map<String, String> problems) {
        appInsights.trackEvent(
            AppInsightsEvent.MEDIATION_REPORT_FAILURE,
            "MILO report " + reportDate,
            problems.toString()
        );
    }
}
