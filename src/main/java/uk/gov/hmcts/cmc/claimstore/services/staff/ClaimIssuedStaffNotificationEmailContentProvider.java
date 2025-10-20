package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.stereotypes.EmailContentProvider;
import java.util.Map;

@Component
public class ClaimIssuedStaffNotificationEmailContentProvider implements EmailContentProvider<Map<String, Object>> {

    private final StaffEmailTemplates staffEmailTemplates;
    private final TemplateService templateService;

    public ClaimIssuedStaffNotificationEmailContentProvider(
        StaffEmailTemplates staffEmailTemplates,
        TemplateService templateService
    ) {
        this.staffEmailTemplates = staffEmailTemplates;
        this.templateService = templateService;
    }

    @Override
    public EmailContent createContent(Map<String, Object> claim) {
        return new EmailContent(
            evaluateTemplate(staffEmailTemplates.getClaimIssuedEmailSubject(), claim),
            staffEmailTemplates.getClaimIssuedEmailBody().trim()
        );
    }

    @Override
    public TemplateService getTemplateService() {
        return templateService;
    }
}
