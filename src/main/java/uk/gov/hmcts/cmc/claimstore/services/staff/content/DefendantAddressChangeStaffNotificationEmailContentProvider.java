package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.ClaimDefendantComparator;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Map;

@Service
public class DefendantAddressChangeStaffNotificationEmailContentProvider {

    private final TemplateService templateService;
    private final StaffEmailTemplates emailTemplates;

    @Autowired
    public DefendantAddressChangeStaffNotificationEmailContentProvider(
        TemplateService templateService,
        StaffEmailTemplates emailTemplates
    ) {
        this.templateService = templateService;
        this.emailTemplates = emailTemplates;
    }

    public EmailContent createContent(Claim claim, ClaimDefendantComparator comparator) {
        Map<String, Object> input = wrapInMap(claim, comparator);

        return new EmailContent(
            evaluateTemplate(emailTemplates.getDefendantAddressChangeEmailSubject(), input),
            evaluateTemplate(emailTemplates.getDefendantAddressChangeEmailBody(), input)
        );
    }

    String evaluateTemplate(String template, Map<String, Object> input) {
        return getTemplateService().evaluate(template, input).trim();
    }

    private Map<String, Object> wrapInMap(Claim claim, ClaimDefendantComparator comparator) {
        return ImmutableMap.<String, Object>builder()
            .put("hasAddressChanged", !comparator.isAddressEqual())
            .put("hasCorrespondenceAddressChanged", !comparator.isCorrespondenceAddressEqual())
            .put("hasPhoneNumberChanged", !comparator.isPhoneNumberEqual())
            .put("claimNumber", claim.getReferenceNumber())
            .build();
    }

    public TemplateService getTemplateService() {
        return templateService;
    }
}
