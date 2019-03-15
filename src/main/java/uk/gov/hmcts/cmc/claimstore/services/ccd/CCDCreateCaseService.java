package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "ccd_enabled")
public class CCDCreateCaseService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public CCDCreateCaseService(CoreCaseDataApi coreCaseDataApi, AuthTokenGenerator authTokenGenerator) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
    }

    @LogExecutionTime
    public StartEventResponse startCreate(
        String authorisation, EventRequestData eventRequestData, boolean isRepresented
    ) {
        if (isRepresented) {
            return coreCaseDataApi.startForCaseworker(
                authorisation,
                authTokenGenerator.generate(),
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                eventRequestData.getEventId()
            );
        }

        return coreCaseDataApi.startForCitizen(
            authorisation,
            authTokenGenerator.generate(),
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            eventRequestData.getEventId()
        );
    }

    @LogExecutionTime
    public CaseDetails submitCreate(
        String authorisation,
        EventRequestData eventRequestData,
        CaseDataContent caseDataContent,
        boolean represented
    ) {
        if (represented) {
            return coreCaseDataApi.submitForCaseworker(
                authorisation,
                this.authTokenGenerator.generate(),
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                eventRequestData.isIgnoreWarning(),
                caseDataContent
            );
        }

        return coreCaseDataApi.submitForCitizen(
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
