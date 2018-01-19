package uk.gov.hmcts.cmc.claimstore.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.JwtService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.exception.InvalidCaseDataException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CCDClaimRepository {
    private static final String JURISDICTION_ID = "CMC";
    private static final String CASE_TYPE_ID = "MoneyClaimCase";

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final CaseMapper caseMapper;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    public CCDClaimRepository(
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        UserService userService,
        CaseMapper caseMapper,
        ObjectMapper objectMapper,
        JwtService jwtService
    ) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.userService = userService;
        this.caseMapper = caseMapper;
        this.objectMapper = objectMapper;
        this.jwtService = jwtService;
    }

    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        return searchClaimsOnCCD(authorisation, ImmutableMap.of("submitterId", submitterId));
    }

    public Optional<Claim> getByClaimReferenceNumber(String referenceNumber, String authorisation) {
        final List<Claim> claims
            = searchClaimsOnCCD(authorisation, ImmutableMap.of("referenceNumber", referenceNumber));

        if (claims.isEmpty()) {
            throw (new NotFoundException("Claim not found by claim reference " + referenceNumber));
        }

        if (claims.size() > 1) {
            throw new RuntimeException("More than one claim found by claim reference " + referenceNumber);
        }

        return claims.stream().findFirst();
    }

    public Optional<Claim> getByClaimExternalId(String externalId, String authorisation) {
        final List<Claim> claims = searchClaimsOnCCD(authorisation, ImmutableMap.of("externalId", externalId));

        if (claims.size() > 1) {
            throw new RuntimeException("More than one claim found by claim externalId " + externalId);
        }

        return claims.stream().findFirst();
    }

    private List<Claim> searchClaimsOnCCD(String authorisation, Map<String, Object> searchString) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        final String serviceAuthToken = this.authTokenGenerator.generate();

        if (jwtService.isCitizen(authorisation)) {

            final List<CaseDetails> result = this.coreCaseDataApi.searchForCitizen(
                authorisation,
                serviceAuthToken,
                userDetails.getId(),
                JURISDICTION_ID,
                CASE_TYPE_ID,
                searchString
            );
            return extractClaims(result);
        } else {

            final List<CaseDetails> result
                = this.coreCaseDataApi.searchForCaseworker(
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
        return result.stream()
            .map(CaseDetails::getData)
            .map(this::convertToCCDCase)
            .map(caseMapper::from)
            .collect(Collectors.toList());
    }

    private CCDCase convertToCCDCase(Map<String, Object> mapData) {
        try {
            final String json = objectMapper.writeValueAsString(mapData);
            return objectMapper.convertValue(json, CCDCase.class);
        } catch (IOException e) {
            throw new InvalidCaseDataException("Failed to serialize to JSON", e);
        }
    }
}
