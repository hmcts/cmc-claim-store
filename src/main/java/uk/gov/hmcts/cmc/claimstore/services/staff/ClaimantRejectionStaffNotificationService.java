package uk.gov.hmcts.cmc.claimstore.services.staff;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.ClaimantDirectionsHearingContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.ClaimantRejectPartAdmissionContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
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
    private final ClaimantDirectionsHearingContentProvider claimantDirectionsHearingContentProvider;

    @Autowired
    public ClaimantRejectionStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties staffEmailProperties,
        StaffPdfCreatorService pdfCreatorService,
        ClaimantRejectPartAdmissionContentProvider claimantRejectPartAdmissionContentProvider,
        ClaimantDirectionsHearingContentProvider claimantDirectionsHearingContentProvider
    ) {
        this.emailService = emailService;
        this.staffEmailProperties = staffEmailProperties;
        this.pdfCreatorService = pdfCreatorService;
        this.claimantRejectPartAdmissionContentProvider = claimantRejectPartAdmissionContentProvider;
        this.claimantDirectionsHearingContentProvider = claimantDirectionsHearingContentProvider;
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

    public void notifyStaffWithClaimantsIntentionToProceed(Claim claim) {
        requireNonNull(claim);

        EmailContent emailContent = claimantDirectionsHearingContentProvider.createContent(getParameters(claim));

        emailService.sendEmail(
            staffEmailProperties.getSender(),
            EmailData.builder()
                .to(staffEmailProperties.getRecipient())
                .subject(emailContent.getSubject())
                .message(emailContent.getBody())
                .build()
        );
    }

    public static Map<String, Object> getParameters(Claim claim) {
        return new ImmutableMap.Builder<String, Object>()
            .put("claimReferenceNumber", claim.getReferenceNumber())
            .put("claimantName", claim.getClaimData().getClaimant().getName())
            .put("defendantName", claim.getClaimData().getDefendant().getName())
            .build();
    }

    public static Map<String, Object> wrapInMap(Claim claim) {
        Map<String, Object> map = new HashMap<>();
        Response defendantResponse = claim.getResponse().orElseThrow(IllegalStateException::new);
        ClaimantResponse claimantResponse = claim.getClaimantResponse().orElseThrow(IllegalStateException::new);

        map.put("claimReferenceNumber", claim.getReferenceNumber());
        map.put("claimantName", claim.getClaimData().getClaimant().getName());
        map.put("defendantName", claim.getClaimData().getDefendant().getName());
        map.put("defendantFreeMediation", defendantResponse.getFreeMediation()
            .orElse(YesNoOption.NO)
            .name()
            .toLowerCase());
        map.put("claimantFreeMediation", ((ResponseRejection) claimantResponse).getFreeMediation()
            .orElse(YesNoOption.NO)
            .name()
            .toLowerCase());

        return map;
    }

    private EmailAttachment createResponsePdfAttachment(Claim claim) {
        requireNonNull(claim);
        return pdfCreatorService.createResponsePdfAttachment(claim);
    }

}
