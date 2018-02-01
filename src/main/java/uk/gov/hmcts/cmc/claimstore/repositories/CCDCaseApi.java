package uk.gov.hmcts.cmc.claimstore.repositories;

import com.google.common.collect.ImmutableMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.claimstore.exceptions.DefendantLinkingException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.JwtHelper;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.UserId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CCDCaseApi {

    public static final String JURISDICTION_ID = "CMC";
    public static final String CASE_TYPE_ID = "MoneyClaimCase";

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final CaseMapper caseMapper;
    private final JsonMapper jsonMapper;
    private final JwtHelper jwtHelper;
    private final CaseAccessApi caseAccessApi;

    public CCDCaseApi(
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        UserService userService,
        CaseMapper caseMapper,
        JsonMapper jsonMapper,
        JwtHelper jwtHelper,
        CaseAccessApi caseAccessApi
    ) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.userService = userService;
        this.caseMapper = caseMapper;
        this.jsonMapper = jsonMapper;
        this.jwtHelper = jwtHelper;
        this.caseAccessApi = caseAccessApi;
    }

    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        return extractClaims(search(authorisation, ImmutableMap.of("case.submitterId", submitterId)));
    }

    public Optional<Claim> getByReferenceNumber(String referenceNumber, String authorisation) {
        List<Claim> claims = extractClaims(
            search(authorisation, ImmutableMap.of("case.referenceNumber", referenceNumber))
        );

        if (claims.size() > 1) {
            throw new CoreCaseDataStoreException("More than one claim found by claim reference " + referenceNumber);
        }

        return claims.isEmpty() ? Optional.empty() : Optional.of(claims.get(0));
    }

    public Optional<Claim> getByExternalId(String externalId, String authorisation) {
        return getCaseDetailsByExternalId(authorisation, externalId)
            .map(CaseDetails::getData)
            .map(this::convertToCCDCase)
            .map((caseMapper::from));
    }

    public Optional<Claim> linkDefendant(String externalId, String defendantId, String authorisation) {
        User user = userService.authenticateAnonymousCaseWorker();
        Optional<CaseDetails> optionalCaseDetails = getCaseDetailsByExternalId(user.getAuthorisation(), externalId);

        if (optionalCaseDetails.isPresent()) {
            CaseDetails caseDetails = optionalCaseDetails.get();
            caseAccessApi.grantAccessToCase(user.getAuthorisation(),
                authTokenGenerator.generate(),
                user.getUserDetails().getId(),
                JURISDICTION_ID,
                CASE_TYPE_ID,
                caseDetails.getId().toString(),
                new UserId(defendantId)
            );

            return Optional.of(readCase(authorisation, caseDetails.getId().toString()));
        }
        return Optional.empty();
    }

    public List<Claim> getByDefendantId(String id, String authorisation) {
        User caseWorker = userService.authenticateAnonymousCaseWorker();
        List<String> caseIdsGivenUserIdHasAccessTo = caseAccessApi.findCaseIdsGivenUserIdHasAccessTo(
            caseWorker.getAuthorisation(),
            authTokenGenerator.generate(),
            caseWorker.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID,
            id
        );

        return caseIdsGivenUserIdHasAccessTo.stream()
            .map((caseId) -> readCase(authorisation, caseId))
            .filter((claim -> !claim.getSubmitterId().equals(id)))
            .collect(Collectors.toList());
    }

    public Optional<Claim> getByLetterHolderId(String id, String authorisation) {
        User user = userService.authenticateAnonymousCaseWorker();

        List<String> letterHolderCases = caseAccessApi.findCaseIdsGivenUserIdHasAccessTo(
            user.getAuthorisation(),
            authTokenGenerator.generate(),
            user.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID,
            id
        );
        if (letterHolderCases.size() > 1) {
            throw new DefendantLinkingException("More than 1 case a letter holder ID has access to found");
        }
        if (letterHolderCases.size() == 0) {
            throw new DefendantLinkingException("No cases found that the letter holder ID has access to");
        }

        return Optional.of(readCase(authorisation, letterHolderCases.get(0)));
    }

    private Claim readCase(String authorisation, String caseId) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        return caseMapper.from(
            convertToCCDCase(
                coreCaseDataApi.readForCitizen(
                    authorisation,
                    authTokenGenerator.generate(),
                    userDetails.getId(),
                    JURISDICTION_ID,
                    CASE_TYPE_ID,
                    caseId
                ).getData()
            )
        );
    }

    private Optional<CaseDetails> getCaseDetailsByExternalId(String authorisation, String externalId) {
        List<CaseDetails> caseResults = search(authorisation, ImmutableMap.of("case.externalId", externalId));
        if (caseResults.size() > 1) {
            throw new CoreCaseDataStoreException("More than one claim found by claim externalId " + externalId);
        }

        return caseResults.isEmpty() ? Optional.empty() : Optional.of(caseResults.get(0));
    }

    private List<CaseDetails> search(String authorisation, Map<String, Object> searchString) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        String serviceAuthToken = this.authTokenGenerator.generate();

        List<CaseDetails> result;
        if (jwtHelper.isSolicitor(authorisation)) {

            result = this.coreCaseDataApi.searchForCaseworker(
                authorisation,
                serviceAuthToken,
                userDetails.getId(),
                JURISDICTION_ID,
                CASE_TYPE_ID,
                searchString
            );

        } else {
            result = this.coreCaseDataApi.searchForCitizen(
                authorisation,
                serviceAuthToken,
                userDetails.getId(),
                JURISDICTION_ID,
                CASE_TYPE_ID,
                searchString
            );

        }
        return result;
    }

    private List<Claim> extractClaims(List<CaseDetails> result) {
        return result.stream()
            .map(CaseDetails::getData)
            .map(this::convertToCCDCase)
            .map(caseMapper::from)
            .collect(Collectors.toList());
    }

    private CCDCase convertToCCDCase(Map<String, Object> mapData) {
        String json = jsonMapper.toJson(mapData);
        return jsonMapper.fromJson(json, CCDCase.class);
    }
}
