package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.MediationCSVGenerationException;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_DATE;

@Service
public class MediationReportService {

    private final EmailService emailService;
    private final CaseSearchApi caseSearchApi;
    private final UserService userService;
    private final AppInsights appInsights;

    private final String emailToAddress;
    private final String emailFromAddress;
    private final Clock clock;

    @Autowired
    public MediationReportService(
        EmailService emailService,
        CaseSearchApi caseSearchApi,
        UserService userService,
        AppInsights appInsights,
        Clock clock,
        @Value("${milo.recipient}") String emailToAddress,
        @Value("${milo.sender}") String emailFromAddress
    ) {
        this.emailService = emailService;
        this.caseSearchApi = caseSearchApi;
        this.userService = userService;
        this.appInsights = appInsights;
        this.clock = clock;
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

    public void automatedMediationReport() throws Exception {
        sendMediationReport(
            userService.authenticateAnonymousCaseWorker().getAuthorisation(),
            LocalDate.now(clock).minusDays(1)
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
        ImmutableMap<String, String> exceptionProperties = ImmutableMap.<String, String>builder()
            .put("MILO report date time", reportDate.format(ISO_DATE))
            .put("Error Message ", e.getMessage())
            .put("Error Stack",
                Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString).collect(Collectors.joining(System.lineSeparator())))
            .build();

        appInsights.trackEvent(AppInsightsEvent.MEDIATION_REPORT_FAILURE, exceptionProperties);
        throw e;
    }

    private void reportMediationExceptions(LocalDate reportDate, Map<String, String> problems) {
        ImmutableMap<String, String> exceptionRecords = ImmutableMap.<String, String>builder()
            .put("MILO report date time", reportDate.format(ISO_DATE))
            .putAll(problems).build();
        appInsights.trackEvent(
            AppInsightsEvent.MEDIATION_REPORT_FAILURE,
            exceptionRecords
        );
    }
}
