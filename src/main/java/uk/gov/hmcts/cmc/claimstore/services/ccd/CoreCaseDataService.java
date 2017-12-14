package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;

@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "core_case_data", havingValue = "true")
public class CoreCaseDataService {
    private final Logger logger = LoggerFactory.getLogger(CoreCaseDataService.class);

    private static final String JURISDICTION_ID = "CMC";
    private static final String CASE_TYPE_ID = "MoneyClaimCase";
    private static final String EVENT_ID = "submitClaimEvent";

    private final SaveCoreCaseDataService saveCoreCaseDataService;
    private final CaseMapper caseMapper;

    @Autowired
    public CoreCaseDataService(
        final SaveCoreCaseDataService saveCoreCaseDataService,
        final CaseMapper caseMapper
    ) {
        this.saveCoreCaseDataService = saveCoreCaseDataService;
        this.caseMapper = caseMapper;
    }

    public CaseDetails save(final String authorisation, final Claim claim) {
        try {
            final CCDCase ccdCase = caseMapper.to(claim);
            final EventRequestData eventRequestData = EventRequestData.builder()
                .userId(claim.getSubmitterId())
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(EVENT_ID)
                .ignoreWarning(true)
                .build();

            return saveCoreCaseDataService.save(authorisation, eventRequestData, ccdCase);
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException("Failed storing claim in CCD store for claim "
                + claim.getReferenceNumber(), exception);
        }
    }
}
