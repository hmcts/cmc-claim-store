package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.stereotypes.EmailContentProvider;

import java.util.Map;

@Service
public class DefendantResponseStaffNotificationEmailContentProvider
    implements EmailContentProvider<Map<String, Object>> {

    private final TemplateService templateService;
    private final StaffEmailTemplates staffEmailTemplates;

    @Autowired
    public DefendantResponseStaffNotificationEmailContentProvider(final TemplateService templateService,
                                                                  final StaffEmailTemplates staffEmailTemplates) {
        this.templateService = templateService;
        this.staffEmailTemplates = staffEmailTemplates;
    }

    @Override
    public EmailContent createContent(final Map<String, Object> input) {
        return new EmailContent(
            evaluateTemplate(staffEmailTemplates.getDefendantResponseEmailSubject(), input),
            evaluateTemplate(staffEmailTemplates.getDefendantResponseEmailBody(), input)
        );
    }

    private String evaluateTemplate(final String template, final Map<String, Object> input) {
        return templateService.evaluate(template, input);
    }


}
