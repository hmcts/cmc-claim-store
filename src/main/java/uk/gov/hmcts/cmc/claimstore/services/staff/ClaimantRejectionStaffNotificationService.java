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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_CLAIMANT_RESPONSE;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_RESPONSE;

@Service
public class ClaimantRejectionStaffNotificationService {

    public static final String CLAIM_REFERENCE_NUMBER = "claimReferenceNumber";
    public static final String CLAIMANT_NAME = "claimantName";
    public static final String DEFENDANT_NAME = "defendantName";
    public static final String DEFENDANT_FREE_MEDIATION = "defendantFreeMediation";
    public static final String CLAIMANT_FREE_MEDIATION = "claimantFreeMediation";

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
                .attachments(emptyList())
                .build()
        );
    }

    public static Map<String, Object> getParameters(Claim claim) {
        return new ImmutableMap.Builder<String, Object>()
            .put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber())
            .put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName())
            .put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName())
            .build();
    }

    public static Map<String, Object> wrapInMap(Claim claim) {
        Map<String, Object> map = new HashMap<>();
        Response defendantResponse = claim.getResponse()
            .orElseThrow(() -> new IllegalStateException(MISSING_RESPONSE));
        ClaimantResponse claimantResponse = claim.getClaimantResponse()
            .orElseThrow(() -> new IllegalStateException(MISSING_CLAIMANT_RESPONSE));

        map.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        map.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        map.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        map.put(DEFENDANT_FREE_MEDIATION, defendantResponse.getFreeMediation()
            .orElse(YesNoOption.NO)
            .name()
            .toLowerCase());
        map.put(CLAIMANT_FREE_MEDIATION, ((ResponseRejection) claimantResponse).getFreeMediation()
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
