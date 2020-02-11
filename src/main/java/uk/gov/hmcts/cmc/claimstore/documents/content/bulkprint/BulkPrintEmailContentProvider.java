package uk.gov.hmcts.cmc.claimstore.documents.content.bulkprint;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.LiveSupportEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.stereotypes.EmailContentProvider;

import java.util.Map;

import static org.apache.commons.lang3.Validate.notEmpty;

@Component
public class BulkPrintEmailContentProvider implements EmailContentProvider<Map<String, Object>> {

    private final TemplateService templateService;
    private final LiveSupportEmailTemplates liveSupportEmailTemplates;

    public BulkPrintEmailContentProvider(
        TemplateService templateService,
        LiveSupportEmailTemplates liveSupportEmailTemplates
    ) {
        this.templateService = templateService;
        this.liveSupportEmailTemplates = liveSupportEmailTemplates;
    }

    @Override
    public EmailContent createContent(Map<String, Object> input) {
        notEmpty(input);

        return new EmailContent(
            evaluateTemplate(liveSupportEmailTemplates.getBulkPrintEmailSubject(), input),
            evaluateTemplate(liveSupportEmailTemplates.getBulkPrintEmailBody(), input)
        );
    }

    @Override
    public TemplateService getTemplateService() {
        return templateService;
    }
}
