package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.stereotypes.EmailContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.apache.commons.lang3.Validate.notEmpty;

@Component
public class IntentionToProceedDeadlineContentProvider implements EmailContentProvider<Map<String, Object>> {

    private final TemplateService templateService;
    private final StaffEmailTemplates staffEmailTemplates;

    public IntentionToProceedDeadlineContentProvider(
        TemplateService templateService,
        StaffEmailTemplates staffEmailTemplates
    ) {
        this.templateService = templateService;
        this.staffEmailTemplates = staffEmailTemplates;
    }

    @Override
    public EmailContent createContent(Map<String, Object> input) {
        notEmpty(input);

        return new EmailContent(
            evaluateTemplate(staffEmailTemplates.getIntentionToProceedDeadlineEmailSubject(), input),
            evaluateTemplate(staffEmailTemplates.getIntentionToProceedDeadlineEmailBody(), input)
        );
    }

    @Override
    public TemplateService getTemplateService() {
        return templateService;
    }

    public static Map<String, Object> getParameters(Claim claim) {
        String dqDeadline = claim.getDirectionsQuestionnaireDeadline()
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy"));

        return ImmutableMap.of(
            "claimantName", claim.getClaimData().getClaimant().getName(),
            "defendantName", claim.getClaimData().getDefendant().getName(),
            "claimReferenceNumber", claim.getReferenceNumber(),
            "dqDeadline", dqDeadline);
    }
}
