package uk.gov.hmcts.cmc.ccd.migration.ccd.services;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.exceptions.CreateCaseException;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.exceptions.OverwriteCaseException;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CoreCaseDataService {

    enum EventType {
        MIGRATED_FROM_CLAIMSTORE_CREATE("MigrationFromClaimstoreCreate"),
        MIGRATED_FROM_CLAIMSTORE_UPDATE("MigrationFromClaimstoreUpdate");

        private String value;

        EventType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }

    public static final String JURISDICTION_ID = "CMC";
    public static final String CASE_TYPE_ID = "MoneyClaimCase";

    private static final Logger logger = LoggerFactory.getLogger(MigrateCoreCaseDataService.class);

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final MigrateCoreCaseDataService migrateCoreCaseDataService;

    @Autowired
    public CoreCaseDataService(
        MigrateCoreCaseDataService migrateCoreCaseDataService,
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator
    ) {
        this.migrateCoreCaseDataService = migrateCoreCaseDataService;
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
    }

    public void create(User user, Claim claim) {
        logger.info("Create case in CCD, claim id = " + claim.getId());
        try {
            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(user.getUserDetails().getId())
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(EventType.MIGRATED_FROM_CLAIMSTORE_CREATE.getValue())
                .ignoreWarning(true)
                .build();

            migrateCoreCaseDataService.save(user.getAuthorisation(), eventRequestData, claim);
        } catch (Exception exception) {
            throw new CreateCaseException(
                String.format("Failed storing claim in CCD store for claim %s", claim.getReferenceNumber()), exception
            );
        }
    }

    public void overwrite(User user, Long ccdId, Claim claim) {
        logger.info("Overwrite " + ccdId + ", claim id = " + claim.getId());
        try {
            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(user.getUserDetails().getId())
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(EventType.MIGRATED_FROM_CLAIMSTORE_UPDATE.getValue())
                .ignoreWarning(true)
                .build();

            migrateCoreCaseDataService.update(user.getAuthorisation(), eventRequestData, ccdId, claim);
        } catch (Exception exception) {
            throw new OverwriteCaseException(
                String.format(
                    "Failed updating claim in CCD store for claim %s on event %s",
                    claim.getReferenceNumber(),
                    EventType.MIGRATED_FROM_CLAIMSTORE_UPDATE), exception
            );
        }
    }

    public Optional<Long> getCcdIdByReferenceNumber(User user, String referenceNumber) {
        logger.info("Get claim from CCD " + referenceNumber);

        Optional<Long> ccdId = search(user, ImmutableMap.of("case.referenceNumber", referenceNumber));
        ccdId.ifPresent(id -> logger.info("Claim found " + id));

        return ccdId;
    }

    private Optional<Long> search(User user, Map<String, Object> searchString) {

        List<CaseDetails> result;
        result = this.coreCaseDataApi.searchForCaseworker(
            user.getAuthorisation(),
            this.authTokenGenerator.generate(),
            user.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID,
            searchString
        );

        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0).getId());
    }
}
