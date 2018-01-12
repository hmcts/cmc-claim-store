package uk.gov.hmcts.cmc.claimstore.events.ccd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;

@Component
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CoreCaseDataUploader {
    private final Logger logger = LoggerFactory.getLogger(CoreCaseDataUploader.class);
    private final CoreCaseDataService coreCaseDataService;

    @Autowired
    public CoreCaseDataUploader(CoreCaseDataService coreCaseDataService) {
        this.coreCaseDataService = coreCaseDataService;
    }

    @TransactionalEventListener
    public void saveRepresentedClaim(RepresentedClaimIssuedEvent event) {
        try {
            coreCaseDataService.save(event.getAuthorisation(), event.getClaim());
        } catch (CoreCaseDataStoreException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @TransactionalEventListener
    public void saveCitizenClaim(ClaimIssuedEvent event) {
        try {
            coreCaseDataService.save(event.getAuthorisation(), event.getClaim());
        } catch (CoreCaseDataStoreException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
}
