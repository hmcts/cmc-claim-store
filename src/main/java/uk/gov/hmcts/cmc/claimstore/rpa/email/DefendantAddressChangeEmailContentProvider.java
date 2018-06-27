package uk.gov.hmcts.cmc.claimstore.rpa.email;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.ClaimDefendantComparator;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.stereotypes.EmailContentProvider;

@Component
public class DefendantAddressChangeEmailContentProvider implements EmailContentProvider<ClaimDefendantComparator> {

    private final EmailTemplates emailTemplates;
    private final TemplateService templateService;

    public DefendantAddressChangeEmailContentProvider(
        EmailTemplates emailTemplates,
        TemplateService templateService
    ) {
        this.emailTemplates = emailTemplates;
        this.templateService = templateService;
    }

    @Override
    public EmailContent createContent(ClaimDefendantComparator comparator) {
        return new EmailContent(
            emailTemplates.getDefendantAddressChangeEmailSubject(),
            emailTemplates.getDefendantAddressChangeEmailBody()
        );
    }

    @Override
    public TemplateService getTemplateService() {
        return templateService;
    }
}
