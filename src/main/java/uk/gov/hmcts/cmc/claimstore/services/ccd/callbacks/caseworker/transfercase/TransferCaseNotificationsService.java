package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.CLAIMANT;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.CaseTransferred.referenceForCaseTransferred;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.DEFENDANT;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.COURT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.EXTERNAL_ID;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.PARTY_NAME;

@Service
public class TransferCaseNotificationsService {

    private static final String CCBC = "County Court Business Centre";

    @Autowired
    private NotificationService notificationService;

    @Value("${notifications.frontendBaseUrl}")
    private String frontendBaseUrl;

    @Value("${notifications.templates.email.caseTransferToCourt}")
    private String caseTransferToCourtTemplate;

    @Value("${notifications.templates.email.caseTransferToCcbcForClaimant}")
    private String caseTransferToCcbcForClaimantTemplate;

    @Value("${notifications.templates.email.caseTransferToCcbcForDefendant}")
    private String caseTransferToCcbcForDefendantTemplate;

    public void sendTransferToCourtEmail(CCDCase ccdCase, Claim claim) {
        String courtName = claim.getTransferContent().getHearingCourtName();
        email(ccdCase, claim, caseTransferToCourtTemplate, caseTransferToCourtTemplate, courtName);
    }

    public void sendTransferToCcbcEmail(CCDCase ccdCase, Claim claim) {
        email(ccdCase, claim, caseTransferToCcbcForClaimantTemplate, caseTransferToCcbcForDefendantTemplate, CCBC);
    }

    private void email(CCDCase ccdCase, Claim claim, String claimantTemplate, String defendantTemplate, String court) {
        String partyName = claim.getClaimData().getClaimant().getName();
        notifyParty(claim, partyName, claim.getSubmitterEmail(), claimantTemplate, court, CLAIMANT);
        if (isDefendantLinked(ccdCase)) {
            partyName = claim.getClaimData().getDefendant().getName();
            notifyParty(claim, partyName, claim.getDefendantEmail(), defendantTemplate, court, DEFENDANT);
        }
    }

    private boolean isDefendantLinked(CCDCase ccdCase) {
        return !isBlank(ccdCase.getRespondents().get(0).getValue().getDefendantId());
    }

    private void notifyParty(Claim claim, String name, String email, String template, String court, String party) {
        notificationService.sendMail(
            email,
            template,
            aggregateParams(claim, name, court, party),
            referenceForCaseTransferred(claim.getReferenceNumber(), party)
        );
    }

    private Map<String, String> aggregateParams(Claim claim, String partyName, String courtName, String party) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PARTY_NAME, partyName);
        parameters.put(CLAIMANT.equals(party) ? CLAIMANT_NAME : DEFENDANT_NAME, partyName);
        parameters.put(COURT_NAME, courtName);
        parameters.put(FRONTEND_BASE_URL, frontendBaseUrl);
        parameters.put(EXTERNAL_ID, claim.getExternalId());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        return parameters;
    }
}
