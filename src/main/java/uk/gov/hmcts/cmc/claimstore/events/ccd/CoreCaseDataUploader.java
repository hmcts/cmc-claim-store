package uk.gov.hmcts.cmc.claimstore.events.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "core_case_data", havingValue = "true")
public class CoreCaseDataUploader {

    private final CoreCaseDataService coreCaseDataService;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public CoreCaseDataUploader(final CoreCaseDataService coreCaseDataService,
                                final AuthTokenGenerator authTokenGenerator) {
        this.coreCaseDataService = coreCaseDataService;
        this.authTokenGenerator = authTokenGenerator;
    }

    @EventListener
    public void saveClaimInCCD(RepresentedClaimIssuedEvent event) {
        final Claim claim = event.getClaim();
        coreCaseDataService.save(event.getAuthorisation(), authTokenGenerator.generate(), claim);
    }
}
