package uk.gov.hmcts.cmc.claimstore.documents.content.settlementagreement;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.stereotypes.EmailContentProvider;

import java.util.Map;

import static org.apache.commons.lang3.Validate.notEmpty;

@Component
public class SettlementAgreementEmailContentProvider implements EmailContentProvider<Map<String, Object>> {

    private final TemplateService templateService;
    private final StaffEmailTemplates staffEmailTemplates;

    public SettlementAgreementEmailContentProvider(
        final TemplateService templateService,
        final StaffEmailTemplates staffEmailTemplates
    ) {
        this.templateService = templateService;
        this.staffEmailTemplates = staffEmailTemplates;
    }

    @Override
    public EmailContent createContent(Map<String, Object> input) {
        notEmpty(input);

        return new EmailContent(
            evaluateTemplate(staffEmailTemplates.getSettlementAgreementEmailSubject(), input).trim(),
            evaluateTemplate(staffEmailTemplates.getSettlementAgreementEmailBody(), input).trim()
        );
    }

    @Override
    public TemplateService getTemplateService() {
        return templateService;
    }
}
