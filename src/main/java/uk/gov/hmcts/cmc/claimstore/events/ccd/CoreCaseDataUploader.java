package uk.gov.hmcts.cmc.claimstore.events.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "core_case_data", havingValue = "true")
public class CoreCaseDataUploader {

    private final CoreCaseDataService coreCaseDataService;

    @Autowired
    public CoreCaseDataUploader(final CoreCaseDataService coreCaseDataService) {
        this.coreCaseDataService = coreCaseDataService;
    }

    @EventListener
    public void saveClaim(RepresentedClaimIssuedEvent event) throws CoreCaseDataStoreException {
        coreCaseDataService.save(event.getAuthorisation(), event.getClaim());
    }
}
