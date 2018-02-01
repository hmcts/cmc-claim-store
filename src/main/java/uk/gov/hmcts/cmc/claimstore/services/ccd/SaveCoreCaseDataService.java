package uk.gov.hmcts.cmc.claimstore.services.ccd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.exception.InvalidCaseDataException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.UserId;

import java.io.IOException;

import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.JURISDICTION_ID;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class SaveCoreCaseDataService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final ObjectMapper objectMapper;
    private final AuthTokenGenerator authTokenGenerator;
    private final CaseAccessApi caseAccessApi;
    private final UserService userService;

    @Autowired
    public SaveCoreCaseDataService(
        CoreCaseDataApi coreCaseDataApi,
        ObjectMapper objectMapper,
        AuthTokenGenerator authTokenGenerator,
        CaseAccessApi caseAccessApi,
        UserService userService
    ) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.objectMapper = objectMapper;
        this.authTokenGenerator = authTokenGenerator;
        this.caseAccessApi = caseAccessApi;
        this.userService = userService;
    }

    public CaseDetails save(
        String authorisation,
        EventRequestData eventRequestData,
        CCDCase ccdCase,
        boolean represented,
        String letterHolderId
    ) {

        StartEventResponse startEventResponse = start(authorisation, eventRequestData, represented);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary("CMC case submission event")
                .description("Submitting CMC case with token " + startEventResponse.getToken())
                .build())
            .data(toJson(ccdCase))
            .build();

        CaseDetails caseDetails = submit(authorisation, eventRequestData, caseDataContent, represented);

        if (letterHolderId != null) {
            User user = userService.authenticateAnonymousCaseWorker();
            caseAccessApi.grantAccessToCase(user.getAuthorisation(),
                authTokenGenerator.generate(),
                user.getUserDetails().getId(),
                JURISDICTION_ID,
                CASE_TYPE_ID,
                caseDetails.getId().toString(),
                new UserId(letterHolderId)
            );
        }
        return caseDetails;
    }

    private CaseDetails submit(
        String authorisation,
        EventRequestData eventRequestData,
        CaseDataContent caseDataContent,
        boolean represented
    ) {
        if (represented) {
            return this.coreCaseDataApi.submitForCaseworker(
                authorisation,
                this.authTokenGenerator.generate(),
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                eventRequestData.isIgnoreWarning(),
                caseDataContent
            );
        } else {
            return this.coreCaseDataApi.submitForCitizen(
                authorisation,
                this.authTokenGenerator.generate(),
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                eventRequestData.isIgnoreWarning(),
                caseDataContent
            );
        }
    }

    private StartEventResponse start(String authorisation, EventRequestData eventRequestData, boolean represented) {
        if (represented) {
            return this.coreCaseDataApi.startForCaseworker(
                authorisation,
                this.authTokenGenerator.generate(),
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                eventRequestData.getEventId()
            );
        } else {
            return this.coreCaseDataApi.startForCitizen(
                authorisation,
                this.authTokenGenerator.generate(),
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                eventRequestData.getEventId());
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
