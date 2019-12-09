package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.content.settlementagreement.SettlementAgreementEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service
public class SettlementReachedStaffNotificationService {

    public static final String FILE_NAME_FORMAT = "%s-settlement-agreement.pdf";

    private final EmailService emailService;
    private final StaffEmailProperties emailProperties;
    private final SettlementAgreementCopyService copyService;
    private final SettlementAgreementEmailContentProvider emailContentProvider;

    @Autowired
    public SettlementReachedStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties emailProperties,
        SettlementAgreementEmailContentProvider emailContentProvider,
        SettlementAgreementCopyService copyService
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.emailContentProvider = emailContentProvider;
        this.copyService = copyService;
    }

    public void notifySettlementReached(Claim claim) {
        EmailContent emailContent = emailContentProvider.createContent(wrapInMap(claim));
        PDF pdf = copyService.createPdf(claim);
        emailService.sendEmail(
            emailProperties.getSender(),
            new EmailData(
                emailProperties.getRecipient(),
                emailContent.getSubject(),
                emailContent.getBody(),
                singletonList(pdf(
                    pdf.getBytes(),
                    pdf.getFilename())
                )
            )
        );
    }

    private static Map<String, Object> wrapInMap(Claim claim) {
        Map<String, Object> map = new HashMap<>();
        map.put("claimReferenceNumber", claim.getReferenceNumber());
        map.put("claimantName", claim.getClaimData().getClaimant().getName());
        map.put("defendantName", claim.getClaimData().getDefendant().getName());
        return map;
    }

}
