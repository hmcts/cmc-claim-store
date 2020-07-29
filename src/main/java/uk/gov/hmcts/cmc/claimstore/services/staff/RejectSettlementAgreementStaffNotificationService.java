package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.RejectSettlementAgreementEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class RejectSettlementAgreementStaffNotificationService {

    private final EmailService emailService;
    private final StaffEmailProperties emailProperties;
    private final RejectSettlementAgreementEmailContentProvider emailContentProvider;

    @Autowired
    public RejectSettlementAgreementStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties emailProperties,
        RejectSettlementAgreementEmailContentProvider emailContentProvider
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.emailContentProvider = emailContentProvider;
    }

    public void notifySettlementRejected(Claim claim) {
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

    public static Map<String, Object> wrapInMap(Claim claim) {
        Map<String, Object> map = new HashMap<>();
        map.put("claimantName", claim.getClaimData().getClaimant().getName());
        map.put("defendantName", claim.getClaimData().getDefendant().getName());
        map.put("claimReferenceNumber", claim.getReferenceNumber());
        return map;
    }

}
