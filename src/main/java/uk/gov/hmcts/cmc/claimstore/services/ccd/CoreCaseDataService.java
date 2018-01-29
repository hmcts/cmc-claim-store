package uk.gov.hmcts.cmc.claimstore.services.ccd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.exception.InvalidCaseDataException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;

import java.io.IOException;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SUBMIT_CLAIM;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CoreCaseDataService {

    private static final String JURISDICTION_ID = "CMC";
    private static final String CASE_TYPE_ID = "MoneyClaimCase";

    private final SaveCoreCaseDataService saveCoreCaseDataService;
    private final UpdateCoreCaseDataService updateCoreCaseDataService;
    private final CaseMapper caseMapper;
    private final ObjectMapper objectMapper;


    @Autowired
    public CoreCaseDataService(
        SaveCoreCaseDataService saveCoreCaseDataService,
        UpdateCoreCaseDataService updateCoreCaseDataService,
        CaseMapper caseMapper,
        ObjectMapper objectMapper
    ) {
        this.saveCoreCaseDataService = saveCoreCaseDataService;
        this.updateCoreCaseDataService = updateCoreCaseDataService;
        this.caseMapper = caseMapper;
        this.objectMapper = objectMapper;
    }

    public CaseDetails save(String authorisation, Claim claim) {
        try {
            CCDCase ccdCase = caseMapper.to(claim);
            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(claim.getSubmitterId())
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(SUBMIT_CLAIM.getValue())
                .ignoreWarning(true)
                .build();

            return saveCoreCaseDataService
                .save(authorisation, eventRequestData, toJson(ccdCase), claim.getClaimData().isClaimantRepresented());
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(String
                .format("Failed storing claim in CCD store for claim %s", claim.getReferenceNumber()), exception);
        }
    }

    public CaseDetails update(String authorisation, CCDCase ccdCase, CaseEvent caseEvent) {
        try {
            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(ccdCase.getSubmitterId())
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(caseEvent.getValue())
                .ignoreWarning(true)
                .build();

            return updateCoreCaseDataService
                .update(authorisation, eventRequestData, ccdCase, ccdCase.getId());
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(String
                .format("Failed storing claim in CCD store for claim %s", ccdCase.getReferenceNumber()), exception);
        }
    }

    private JsonNode toJson(CCDCase ccdCase) {
        try {
            JsonNode dataNode = objectMapper.readTree(objectMapper.writeValueAsString(ccdCase));
            return objectMapper.convertValue(dataNode, JsonNode.class);
        } catch (IOException e) {
            throw new InvalidCaseDataException("Failed to serialize to JSON", e);
        }
    }
}
