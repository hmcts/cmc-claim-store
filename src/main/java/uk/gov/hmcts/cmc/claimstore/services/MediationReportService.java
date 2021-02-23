package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    Logger logger = LoggerFactory.getLogger(this.getClass());

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

    public void sendMediationReport(String authorisation, LocalDate mediationDate,
                                    String emailToAddressFromSupportController) {
        try {
            MediationCSVGenerator generator = new MediationCSVGenerator(caseSearchApi, mediationDate, authorisation);
            generator.createMediationCSV();
            String csvData = generator.getCsvData();

            emailService.sendEmail(emailFromAddress,
                prepareMediationEmailData(csvData, mediationDate, emailToAddressFromSupportController));

            Map<String, String> problematicRecords = generator.getProblematicRecords();
            if (!problematicRecords.isEmpty()) {
                logger.info("MILO: problematicRecords count is {}", problematicRecords.size());
                reportMediationExceptions(mediationDate, problematicRecords);
            }
        } catch (MediationCSVGenerationException e) {
            reportMediationException(e, mediationDate);
        }
    }

    public void automatedMediationReport() throws Exception {
        logger.info("MILO: Triggering MILO report for {}", LocalDate.now(clock).minusDays(1));
        sendMediationReport(
            userService.authenticateAnonymousCaseWorker().getAuthorisation(),
            LocalDate.now(clock).minusDays(1), ""
        );
    }

    private EmailData prepareMediationEmailData(String mediationCSV,
                                                LocalDate mediationDate, String emailToAddressFromSupportController) {
        String fileName = "MediationCSV." + mediationDate.toString() + ".csv";
        EmailAttachment mediationCSVAttachment = EmailAttachment.csv(mediationCSV.getBytes(), fileName);
        if (!StringUtils.isBlank(emailToAddressFromSupportController)) {
            logger.info("MILO: Sending email to support controller mail address");
            return new EmailData(
                emailToAddressFromSupportController,
                "MediationCSV " + mediationDate.toString(),
                "OCMC mediation" + mediationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                Collections.singletonList(mediationCSVAttachment)
            );
        } else {
            logger.info("MILO: Sending email to mediation team");
            return new EmailData(
                emailToAddress,
                "MediationCSV " + mediationDate.toString(),
                "OCMC mediation" + mediationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                Collections.singletonList(mediationCSVAttachment)
            );
        }
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
