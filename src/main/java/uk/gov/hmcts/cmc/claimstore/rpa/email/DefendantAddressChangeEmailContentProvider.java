package uk.gov.hmcts.cmc.claimstore.rpa.email;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.ClaimDefendantComparator;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.stereotypes.EmailContentProvider;

import java.util.Map;

@Component
public class DefendantAddressChangeEmailContentProvider implements EmailContentProvider<ClaimDefendantComparator> {

    private final EmailTemplates emailTemplates;
    private final TemplateService templateService;

    public DefendantAddressChangeEmailContentProvider (
        EmailTemplates emailTemplates,
        TemplateService templateService
    ) {
        this.emailTemplates = emailTemplates;
        this.templateService = templateService;
    }

    @Override
    public EmailContent createContent(ClaimDefendantComparator comparator) {
        return new EmailContent(
            evaluateTemplate(emailTemplates.getDefendantAddressChangeEmailSubject(), wrapInMap(comparator)),
            emailTemplates.getDefendantAddressChangeEmailBody()
        );
    }

    private Map<String, Object> wrapInMap(ClaimDefendantComparator comparator) {
        return ImmutableMap.<String, Object>builder()
            .put("hasAddressChanged", comparator.isDefendantAddressEqual())
            .put("hasCorrespondenceAddressChanged", comparator.isDefendantCorrespondenceAddressEqual())
            .put("hasPhoneNumberChanged", false) // todo how to check this?
            .build();
    }

    @Override
    public TemplateService getTemplateService() {
        return templateService;
    }
}
