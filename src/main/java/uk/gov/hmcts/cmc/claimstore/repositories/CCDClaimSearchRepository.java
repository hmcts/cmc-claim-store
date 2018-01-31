package uk.gov.hmcts.cmc.claimstore.repositories;

import com.google.common.collect.ImmutableMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.JwtHelper;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CCDClaimSearchRepository {

    public static final String JURISDICTION_ID = "CMC";
    public static final String CASE_TYPE_ID = "MoneyClaimCase";

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final CaseMapper caseMapper;
    private final JsonMapper jsonMapper;
    private final JwtHelper jwtHelper;

    public CCDClaimSearchRepository(
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        UserService userService,
        CaseMapper caseMapper,
        JsonMapper jsonMapper,
        JwtHelper jwtHelper
    ) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.userService = userService;
        this.caseMapper = caseMapper;
        this.jsonMapper = jsonMapper;
        this.jwtHelper = jwtHelper;
    }

    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        return search(authorisation, ImmutableMap.of("case.submitterId", submitterId));
    }

    public Optional<Claim> getByReferenceNumber(String referenceNumber, String authorisation) {
        List<Claim> claims = search(authorisation, ImmutableMap.of("case.referenceNumber", referenceNumber));

        if (claims.size() > 1) {
            throw new CoreCaseDataStoreException("More than one claim found by claim reference " + referenceNumber);
        }

        return claims.isEmpty() ? Optional.empty() : Optional.of(claims.get(0));
    }

    public Optional<Claim> getByExternalId(String externalId, String authorisation) {
        final List<Claim> claims = search(authorisation, ImmutableMap.of("case.externalId", externalId));

        if (claims.size() > 1) {
            throw new CoreCaseDataStoreException("More than one claim found by claim externalId " + externalId);
        }

        return claims.isEmpty() ? Optional.empty() : Optional.of(claims.get(0));
    }

    private List<Claim> search(String authorisation, Map<String, Object> searchString) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        String serviceAuthToken = this.authTokenGenerator.generate();

        if (jwtHelper.isSolicitor(authorisation)) {

            List<CaseDetails> result = this.coreCaseDataApi.searchForCaseworker(
                authorisation,
                serviceAuthToken,
                userDetails.getId(),
                JURISDICTION_ID,
                CASE_TYPE_ID,
                searchString
            );

            return extractClaims(result);
        } else {

            List<CaseDetails> result
                = this.coreCaseDataApi.searchForCitizen(
                authorisation,
                serviceAuthToken,
                userDetails.getId(),
                JURISDICTION_ID,
                CASE_TYPE_ID,
                searchString
            );

            return extractClaims(result);
        }
    }

    private List<Claim> extractClaims(List<CaseDetails> result) {

        Map<Long, Map<String, Object>> collectMap = result.stream()
            .collect(Collectors.toMap(CaseDetails::getId, CaseDetails::getData));

        return collectMap.entrySet().stream()
            .map(this::mapToClaim)
            .collect(Collectors.toList());
    }

    private CCDCase convertToCCDCase(Map<String, Object> mapData) {
        String json = jsonMapper.toJson(mapData);
        return jsonMapper.fromJson(json, CCDCase.class);
    }

    private Claim mapToClaim(Map.Entry<Long, Map<String, Object>> entry) {
        Map<String, Object> entryValue = entry.getValue();
        entryValue.put("id", entry.getKey());

        CCDCase ccdCase = convertToCCDCase(entryValue);
        return caseMapper.from(ccdCase);
    }
}
