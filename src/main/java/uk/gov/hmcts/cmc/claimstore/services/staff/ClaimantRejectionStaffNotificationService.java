package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.ClaimantRejectPartAdmissionContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

@Service
public class ClaimantRejectionStaffNotificationService {

    private final EmailService emailService;
    private final StaffEmailProperties staffEmailProperties;
    private final StaffPdfCreatorService pdfCreatorService;
    private final ClaimantRejectPartAdmissionContentProvider claimantRejectPartAdmissionContentProvider;

    @Autowired
    public ClaimantRejectionStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties staffEmailProperties,
        StaffPdfCreatorService pdfCreatorService,
        ClaimantRejectPartAdmissionContentProvider claimantRejectPartAdmissionContentProvider
    ) {
        this.emailService = emailService;
        this.staffEmailProperties = staffEmailProperties;
        this.pdfCreatorService = pdfCreatorService;
        this.claimantRejectPartAdmissionContentProvider = claimantRejectPartAdmissionContentProvider;
    }

    public void notifyStaffClaimantRejectPartAdmission(Claim claim) {
        requireNonNull(claim);
        requireNonNull(claim.getClaimantRespondedAt());

        EmailContent emailContent = claimantRejectPartAdmissionContentProvider.createContent(wrapInMap(claim));

        emailService.sendEmail(
            staffEmailProperties.getSender(),
            new EmailData(
                staffEmailProperties.getRecipient(),
                emailContent.getSubject(),
                emailContent.getBody(),
                singletonList(createResponsePdfAttachment(claim))
            )
        );
    }

    public static Map<String, Object> wrapInMap(Claim claim) {
        Map<String, Object> map = new HashMap<>();
        Response defendantResponse = claim.getResponse().orElseThrow(IllegalStateException::new);
        ClaimantResponse claimantResponse = claim.getClaimantResponse().orElseThrow(IllegalStateException::new);
        System.out.println(defendantResponse.getFreeMediation().get());
        System.out.println(((ResponseRejection) claimantResponse).getFreeMediation());
        map.put("claimReferenceNumber", claim.getReferenceNumber());
        map.put("claimantName", claim.getClaimData().getClaimant().getName());
        map.put("defendantName", claim.getClaimData().getDefendant().getName());
        map.put("defendantFreeMediation", defendantResponse.getFreeMediation().get());
        map.put("claimantFreeMediation", ((ResponseRejection) claimantResponse).getFreeMediation().get());

        return map;
    }

    private EmailAttachment createResponsePdfAttachment(Claim claim) {
        requireNonNull(claim);
        return pdfCreatorService.createResponsePdfAttachment(claim);
    }

}
