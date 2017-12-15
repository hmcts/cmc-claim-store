package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.stereotypes.EmailContentProvider;

import java.util.Map;

import static org.apache.commons.lang3.Validate.notEmpty;

@Component
public class RequestSubmittedNotificationEmailContentProvider implements EmailContentProvider<Map<String, Object>> {

    private final TemplateService templateService;
    private final StaffEmailTemplates emailTemplates;

    public RequestSubmittedNotificationEmailContentProvider(
        TemplateService templateService,
        StaffEmailTemplates emailTemplates
    ) {
        this.templateService = templateService;
        this.emailTemplates = emailTemplates;
    }

    @Override
    public EmailContent createContent(Map<String, Object> input) {
        notEmpty(input);

        return new EmailContent(
            evaluateTemplate(emailTemplates.getCCJRequestSubmittedEmailSubject(), input),
            evaluateTemplate(emailTemplates.getCCJRequestSubmittedEmailBody(), input)
        );
    }

    @Override
    public TemplateService getTemplateService() {
        return templateService;
    }
}
