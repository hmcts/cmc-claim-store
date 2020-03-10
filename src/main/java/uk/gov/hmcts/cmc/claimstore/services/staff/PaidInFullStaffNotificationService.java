package uk.gov.hmcts.cmc.claimstore.services.staff;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public PaidInFullStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties emailProperties,
        PaidInFullStaffEmailContentProvider emailContentProvider
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.emailContentProvider = emailContentProvider;
    }

    public void notifyPaidInFull(Claim claim) {
        EmailContent emailContent = emailContentProvider.createContent(wrapInMap(claim));
        emailService.sendEmail(
            emailProperties.getSender(),
            new EmailData(
                emailProperties.getRecipient(),
                emailContent.getSubject(),
                emailContent.getBody(),
                Collections.emptyList()
            )
        );
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
