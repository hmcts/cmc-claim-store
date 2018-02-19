package uk.gov.hmcts.cmc.ccd.migration.ccd.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.migration.ccd.repositories.CCDCaseApi;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.migration.ccd.repositories.CCDCaseApi.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.ccd.migration.ccd.repositories.CCDCaseApi.JURISDICTION_ID;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CoreCaseDataService {

    enum EventType {
        MIGRATED_FROM_CLAIMSTORE("MigrationFromClaimstoreEvent");

        private String value;

        EventType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }

    private final MigrateCoreCaseDataService migrateCoreCaseDataService;
    private final CCDCaseApi ccdCaseApi;

    @Autowired
    public CoreCaseDataService(
        MigrateCoreCaseDataService migrateCoreCaseDataService,
        CCDCaseApi ccdCaseApi
    ) {
        this.migrateCoreCaseDataService = migrateCoreCaseDataService;
        this.ccdCaseApi = ccdCaseApi;
    }

    public void create(User user, Claim claim) {
        try {
            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(user.getUserDetails().getId())
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(EventType.MIGRATED_FROM_CLAIMSTORE.getValue())
                .ignoreWarning(true)
                .build();

            migrateCoreCaseDataService.save(
                user.getAuthorisation(),
                eventRequestData,
                claim
            );
        } catch (Exception exception) {
            throw new RuntimeException(
                String.format("Failed storing claim in CCD store for claim %s", claim.getReferenceNumber()), exception
            );
        }
    }

    public void overwrite(User user, Long ccdId, Claim claim) {
        try {
            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(user.getUserDetails().getId())
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(EventType.MIGRATED_FROM_CLAIMSTORE.getValue())
                .ignoreWarning(true)
                .build();

            migrateCoreCaseDataService.update(user.getAuthorisation(), eventRequestData, ccdId, claim);
        } catch (Exception exception) {
            throw new RuntimeException(
                String.format(
                    "Failed updating claim in CCD store for claim %s on event %s",
                    claim.getReferenceNumber(),
                    EventType.MIGRATED_FROM_CLAIMSTORE
                ),
                exception
            );
        }
    }

    public Optional<Long> claimExists(User user, String referenceNUmber) {
        return ccdCaseApi.claimExists(user, referenceNUmber);
    }
}
