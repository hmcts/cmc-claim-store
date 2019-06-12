package uk.gov.hmcts.cmc.ccd.migration.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.UpdateCCDCaseService;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;

import java.util.concurrent.atomic.AtomicInteger;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SUPPORT_UPDATE;

@Service
public class SupportUpdateService {

    private static final Logger logger = LoggerFactory.getLogger(SupportUpdateService.class);

    private final UpdateCCDCaseService updateCCDCaseService;
    private final CaseMapper caseMapper;
    private final boolean dryRun;

    @Autowired
    public SupportUpdateService(
        UpdateCCDCaseService updateCCDCaseService,
        CaseMapper caseMapper,
        @Value("${migration.dryRun:true}") boolean dryRun
    ) {
        this.updateCCDCaseService = updateCCDCaseService;
        this.caseMapper = caseMapper;
        this.dryRun = dryRun;
    }

    public void updateCase(
        User user,
        AtomicInteger updatedClaims,
        AtomicInteger failedOnUpdates,
        CCDCase ccdCase
    ) {
        String caseReference = ccdCase.getPreviousServiceCaseReference();
        try {
            logger.info("start updating case for: {} for event: {} counter: {}",
                caseReference,
                SUPPORT_UPDATE,
                updatedClaims.toString());
            if (!dryRun) {
                updateCCDCaseService.updateCase(
                    user,
                    ccdCase.getId(),
                    caseMapper.from(ccdCase),
                    SUPPORT_UPDATE
                );
            }
            updatedClaims.incrementAndGet();

        } catch (Exception exception) {
            logger.info("Claim update for events failed for Claim reference {} due to {}",
                caseReference,
                exception.getMessage(),
                exception
            );
            failedOnUpdates.incrementAndGet();
        }

    }
}
