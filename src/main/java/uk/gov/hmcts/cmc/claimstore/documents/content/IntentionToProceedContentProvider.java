package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.stereotypes.EmailContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.Validate.notEmpty;

@Component
public class IntentionToProceedContentProvider implements EmailContentProvider<Map<String, Object>> {

    private final TemplateService templateService;
    private final StaffEmailTemplates staffEmailTemplates;

    public IntentionToProceedContentProvider(TemplateService templateService, StaffEmailTemplates staffEmailTemplates) {
        this.templateService = templateService;
        this.staffEmailTemplates = staffEmailTemplates;
    }

    @Override
    public EmailContent createContent(Map<String, Object> input) {
        notEmpty(input);

        return new EmailContent(
            evaluateTemplate(staffEmailTemplates.getIntentionToProceedEmailSubject(), input),
            evaluateTemplate(staffEmailTemplates.getIntentionToProceedEmailBody(), input)
        );
    }

    public Map<String, Object> createParameters(Collection<Claim> failedClaims) {
        return ImmutableMap.of(
            "noOfClaims", failedClaims.size(),
            "claimIds", failedClaims.stream().map(c -> c.getId().toString())
                .collect(Collectors.joining("\n"))
        );
    }

    @Override
    public TemplateService getTemplateService() {
        return templateService;
    }
}
