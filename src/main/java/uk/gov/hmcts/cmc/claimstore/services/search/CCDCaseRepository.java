package uk.gov.hmcts.cmc.claimstore.services.search;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Service("caseRepository")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CCDCaseRepository implements CaseRepository {
    private final CCDCaseApi ccdCaseApi;

    public CCDCaseRepository(
        CCDCaseApi ccdCaseApi
    ) {
        this.ccdCaseApi = ccdCaseApi;
    }

    @Override
    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        return ccdCaseApi.getBySubmitterId(submitterId, authorisation);
    }

    @Override
    public Optional<Claim> getClaimByExternalId(String externalId, String authorisation) {
        return ccdCaseApi.getByExternalId(externalId, authorisation);
    }

    @Override
    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        return ccdCaseApi.getByReferenceNumber(claimReferenceNumber, authorisation);
    }

    @Override
    public Claim linkDefendant(String externalId, String defendantId, String authorisation) {
        return ccdCaseApi.linkDefendant(externalId, defendantId, authorisation);
    }

    @Override
    public List<Claim> getByDefendantId(String id, String authorisation) {
        return ccdCaseApi.getByDefendantId(id, authorisation);
    }

    @Override
    public Optional<Claim> getByLetterHolderId(String id, String authorisation) {
        return ccdCaseApi.getByLetterHolderId(id, authorisation);
    }
}
