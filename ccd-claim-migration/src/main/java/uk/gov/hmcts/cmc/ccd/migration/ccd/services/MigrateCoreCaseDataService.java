package uk.gov.hmcts.cmc.ccd.migration.ccd.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.idam.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.UserId;

import static uk.gov.hmcts.cmc.ccd.migration.ccd.services.CoreCaseDataService.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.ccd.migration.ccd.services.CoreCaseDataService.JURISDICTION_ID;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class MigrateCoreCaseDataService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final CaseAccessApi caseAccessApi;
    private final UserService userService;
    private final CaseMapper caseMapper;

    @Autowired
    public MigrateCoreCaseDataService(
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        CaseAccessApi caseAccessApi,
        UserService userService,
        CaseMapper caseMapper
    ) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.caseAccessApi = caseAccessApi;
        this.userService = userService;
        this.caseMapper = caseMapper;
    }

    public void update(
        String authorisation,
        EventRequestData eventRequestData,
        Long ccdId,
        Claim claim
    ) {
        CCDCase ccdCase = caseMapper.to(claim);

        StartEventResponse startEventResponse = startEvent(authorisation, eventRequestData, ccdId);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary("Migrating case")
                    .description("Migrating case - overwriting existing record")
                    .build()
            ).data(ccdCase)
            .build();

        CaseDetails caseDetails = submitEvent(authorisation, eventRequestData, caseDataContent, ccdId);

        grantAccessToCase(caseDetails.getId(), claim);
    }

    public void save(
        String authorisation, EventRequestData eventRequestData, Claim claim
    ) {
        CCDCase ccdCase = caseMapper.to(claim);

        StartEventResponse startEventResponse = start(authorisation, eventRequestData);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary("Migrating case")
                    .description("Migrating case - create new record")
                    .build()
            ).data(ccdCase)
            .build();

        CaseDetails caseDetails = submit(authorisation, eventRequestData, caseDataContent);

        grantAccessToCase(caseDetails.getId(), claim);
    }

    private void grantAccessToCase(Long ccdId, Claim claim) {
        // make sure both submitter and defendant (or letterHolder) can access the case
        grantAccess(ccdId.toString(), claim.getSubmitterId());
        // only claims created by citizen have this fields populated
        if (!claim.getClaimData().isClaimantRepresented()) {
            String defendantId = claim.getDefendantId() != null ? claim.getDefendantId() : claim.getLetterHolderId();
            grantAccess(ccdId.toString(), defendantId);
        }

    }

    private void grantAccess(
        String caseId, String userId
    ) {
        User user = userService.getUser(userService.authenticateAnonymousCaseworker());

        caseAccessApi.grantAccessToCase(
            user.getAuthorisation(),
            authTokenGenerator.generate(),
            user.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID,
            caseId,
            new UserId(userId)
        );
    }

    private CaseDetails submit(
        String authorisation, EventRequestData eventRequestData, CaseDataContent caseDataContent
    ) {
        return coreCaseDataApi.submitForCaseworker(
            authorisation,
            authTokenGenerator.generate(),
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            eventRequestData.isIgnoreWarning(),
            caseDataContent
        );
    }

    private StartEventResponse start(String authorisation, EventRequestData eventRequestData) {
        return this.coreCaseDataApi.startForCaseworker(
            authorisation,
            this.authTokenGenerator.generate(),
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            eventRequestData.getEventId()
        );
    }

    private StartEventResponse startEvent(String authorisation, EventRequestData eventRequestData, Long caseId) {

        return this.coreCaseDataApi.startEventForCaseWorker(
            authorisation,
            this.authTokenGenerator.generate(),
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            caseId.toString(),
            eventRequestData.getEventId()
        );
    }

    private CaseDetails submitEvent(
        String authorisation,
        EventRequestData eventRequestData,
        CaseDataContent caseDataContent,
        Long caseId
    ) {

        return this.coreCaseDataApi.submitEventForCaseWorker(
            authorisation,
            this.authTokenGenerator.generate(),
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            caseId.toString(),
            eventRequestData.isIgnoreWarning(),
            caseDataContent
        );
    }
}
