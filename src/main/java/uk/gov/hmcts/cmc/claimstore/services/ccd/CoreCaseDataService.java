package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;

import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.JURISDICTION_ID;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CoreCaseDataService {

    private static final String EVENT_ID = "submitClaimEvent";

    private final SaveCoreCaseDataService saveCoreCaseDataService;
    private final CaseMapper caseMapper;

    @Autowired
    public CoreCaseDataService(
        SaveCoreCaseDataService saveCoreCaseDataService,
        CaseMapper caseMapper
    ) {
        this.saveCoreCaseDataService = saveCoreCaseDataService;
        this.caseMapper = caseMapper;
    }

    public CaseDetails save(String authorisation, Claim claim) {
        try {
            CCDCase ccdCase = caseMapper.to(claim);
            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(claim.getSubmitterId())
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(EVENT_ID)
                .ignoreWarning(true)
                .build();

            return saveCoreCaseDataService
                .save(
                    authorisation,
                    eventRequestData,
                    ccdCase,
                    claim.getClaimData().isClaimantRepresented(),
                    claim.getLetterHolderId()
                );
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(String
                .format("Failed storing claim in CCD store for claim %s", claim.getReferenceNumber()), exception);
        }
    }
}
