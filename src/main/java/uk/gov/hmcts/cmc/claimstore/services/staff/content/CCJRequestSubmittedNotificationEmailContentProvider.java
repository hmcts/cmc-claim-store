package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.stereotypes.EmailContentProvider;

import java.util.Map;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

@Component
public class CCJRequestSubmittedNotificationEmailContentProvider implements EmailContentProvider<Map<String, Object>> {

    private final TemplateService templateService;
    private final StaffEmailTemplates staffEmailTemplates;

    public CCJRequestSubmittedNotificationEmailContentProvider(
        TemplateService templateService,
        StaffEmailTemplates staffEmailTemplates
    ) {
        this.templateService = templateService;
        this.staffEmailTemplates = staffEmailTemplates;
    }

    @Override
    public EmailContent createContent(final Map<String, Object> input) {
        notNull(input);
        notEmpty(input);

        return new EmailContent(
            evaluateTemplate(staffEmailTemplates.getCCJRequestSubmittedEmailSubject(), input),
            evaluateTemplate(staffEmailTemplates.getCCJRequestSubmittedEmailBody(), input)
        );
    }

    private String evaluateTemplate(final String template, final Map<String, Object> input) {
        return templateService.evaluate(template, input);
    }
}
