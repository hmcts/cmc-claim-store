package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.stereotypes.EmailContentProvider;

import java.util.Map;

import static org.apache.commons.lang3.Validate.notEmpty;

@Component
public class ChangeContactDetailsEmailContentProvider implements EmailContentProvider<Map<String, Object>> {

    private final TemplateService templateService;
    private final EmailTemplates emailTemplates;

    public ChangeContactDetailsEmailContentProvider(
            TemplateService templateService,
            EmailTemplates emailTemplates
    ) {
        this.templateService = templateService;
        this.emailTemplates = emailTemplates;
    }

    @Override
    public EmailContent createContent(Map<String, Object> input) {
        notEmpty(input);

        return new EmailContent(
                evaluateTemplate(emailTemplates.getContactChangeEmailBody(), input),
                evaluateTemplate(emailTemplates.getContactChangeEmailSubject(), input)
        );
    }

    @Override
    public TemplateService getTemplateService() {
        return templateService;
    }
}
