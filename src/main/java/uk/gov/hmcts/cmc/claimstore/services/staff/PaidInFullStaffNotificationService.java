package uk.gov.hmcts.cmc.claimstore.services.staff;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.PaidInFullStaffEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

@Service
public class PaidInFullStaffNotificationService {

    private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EmailService emailService;
    private final StaffEmailProperties emailProperties;
    private final PaidInFullStaffEmailContentProvider emailContentProvider;
    private final boolean staffEmailsEnabled;

    @Autowired
    public PaidInFullStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties emailProperties,
        PaidInFullStaffEmailContentProvider emailContentProvider,
        @Value("${feature_toggles.staff_emails_enabled}") boolean staffEmailsEnabled
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.emailContentProvider = emailContentProvider;
        this.staffEmailsEnabled = staffEmailsEnabled;
    }

    public void notifyPaidInFull(Claim claim) {
        if (staffEmailsEnabled && claim.getClaimData().isClaimantRepresented()) {
            EmailContent emailContent = emailContentProvider.createContent(wrapInMap(claim));
            emailService.sendEmail(
                emailProperties.getSender(),
                new EmailData(
                    emailProperties.getLegalRecipient(),
                    emailContent.getSubject(),
                    emailContent.getBody(),
                    Collections.emptyList()
                )
            );
        }
    }

    private static Map<String, Object> wrapInMap(Claim claim) {
        return ImmutableMap.<String, Object>builder()
            .put("claimReferenceNumber", claim.getReferenceNumber())
            .put("claimantName", claim.getClaimData().getClaimant().getName())
            .put("defendantName", claim.getClaimData().getDefendant().getName())
            .put("moneyReceivedOn", claim.getMoneyReceivedOn()
                .orElseThrow(() -> new IllegalArgumentException("Missing money received date"))
                .format(df))
            .build();
    }
}
