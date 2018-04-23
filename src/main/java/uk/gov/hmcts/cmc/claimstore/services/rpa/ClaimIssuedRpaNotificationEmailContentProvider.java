package uk.gov.hmcts.cmc.claimstore.services.rpa;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.RpaEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.stereotypes.EmailContentProvider;

import java.util.Map;

@Component
public class ClaimIssuedRpaNotificationEmailContentProvider implements EmailContentProvider<Map<String, Object>> {

    private final RpaEmailTemplates rpaEmailTemplates;
    private final TemplateService templateService;

    public ClaimIssuedRpaNotificationEmailContentProvider(
        RpaEmailTemplates rpaEmailTemplates,
        TemplateService templateService
    ) {
        this.rpaEmailTemplates = rpaEmailTemplates;
        this.templateService = templateService;
    }

    @Override
    public EmailContent createContent(Map<String, Object> claim) {
        return new EmailContent(
            evaluateTemplate(rpaEmailTemplates.getClaimIssuedEmailSubject(), claim),
            null
        );
    }

    @Override
    public TemplateService getTemplateService() {
        return templateService;
    }
}
