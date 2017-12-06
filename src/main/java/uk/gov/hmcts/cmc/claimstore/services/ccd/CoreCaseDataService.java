package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.client.SaveCaseService;
import uk.gov.hmcts.cmc.ccd.client.model.CaseDetails;
import uk.gov.hmcts.cmc.ccd.client.model.EventRequestData;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "core_case_data", havingValue = "true")
public class CoreCaseDataService {

    private static final String JURISDICTION_ID = "CMC";
    private static final String CASE_TYPE_ID = "MoneyClaimCase";
    private static final String EVENT_ID = "submitClaimEvent";

    private final SaveCaseService saveCaseService;
    private final CaseMapper caseMapper;

    @Autowired
    public CoreCaseDataService(final SaveCaseService saveCaseService, final CaseMapper caseMapper) {
        this.saveCaseService = saveCaseService;
        this.caseMapper = caseMapper;
    }

    public CaseDetails save(final String authorisation, final String serviceAuthorisation, final Claim claim) {
        final CCDCase ccdCase = caseMapper.to(claim);
        final EventRequestData eventRequestData = EventRequestData.builder()
            .userId(claim.getSubmitterId())
            .jurisdictionId(JURISDICTION_ID)
            .caseTypeId(CASE_TYPE_ID)
            .eventId(EVENT_ID)
            .ignoreWarning(true)
            .build();

        return saveCaseService.save(authorisation, serviceAuthorisation, eventRequestData, ccdCase);
    }
}
