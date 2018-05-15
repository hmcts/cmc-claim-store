package uk.gov.hmcts.cmc.claimstore.services.rpa;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.RpaEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.stereotypes.EmailContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Map;

@Component
public class ClaimIssuedRpaNotificationEmailContentProvider implements EmailContentProvider<Claim> {

    private final RpaEmailTemplates emailTemplates;
    private final TemplateService templateService;

    public ClaimIssuedRpaNotificationEmailContentProvider(
        RpaEmailTemplates emailTemplates,
        TemplateService templateService
    ) {
        this.emailTemplates = emailTemplates;
        this.templateService = templateService;
    }

    @Override
    public EmailContent createContent(Claim claim) {
        return new EmailContent(
            evaluateTemplate(emailTemplates.getClaimIssuedEmailSubject(), wrapInMap(claim)),
            emailTemplates.getClaimIssuedEmailBody()
        );
    }

    private Map<String, Object> wrapInMap(Claim claim) {
        return ImmutableMap.<String, Object>builder()
            .put("claimReferenceNumber", claim.getReferenceNumber())
            .build();
    }

    @Override
    public TemplateService getTemplateService() {
        return templateService;
    }
}
