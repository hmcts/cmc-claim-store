package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class SaveCoreCaseDataService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public SaveCoreCaseDataService(
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator
    ) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
    }

    public CaseDetails save(
        String authorisation,
        EventRequestData eventRequestData,
        Object data,
        boolean represented
    ) {

        StartEventResponse startEventResponse = start(authorisation, eventRequestData, represented);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary("CMC case submission event")
                .description("Submitting CMC case with token " + startEventResponse.getToken())
                .build())
            .data(data)
            .build();

        return submit(authorisation, eventRequestData, caseDataContent, represented);
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
}
