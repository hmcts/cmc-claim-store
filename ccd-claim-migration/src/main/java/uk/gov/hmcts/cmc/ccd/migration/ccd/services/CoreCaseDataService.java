package uk.gov.hmcts.cmc.ccd.migration.ccd.services;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.exceptions.CreateCaseException;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.exceptions.OverwriteCaseException;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.CaseEvent;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
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
        for (CaseEvent event : CaseEvent.values()) {
            if (eventHasBeenPerformedOnClaim(event, claim)) {
                logger.info("Create case in CCD, claim id = %d, event = %s", claim.getId(), event.getValue());
                try {

                    EventRequestData eventRequestData = EventRequestData.builder()
                        .userId(user.getUserDetails().getId())
                        .jurisdictionId(JURISDICTION_ID)
                        .caseTypeId(CASE_TYPE_ID)
                        .eventId(event.getValue())
                        .ignoreWarning(true)
                        .build();

                    migrateCoreCaseDataService.save(user.getAuthorisation(), eventRequestData, claim);
                } catch (Exception exception) {
                    throw new CreateCaseException(
                        String.format("Failed storing claim in CCD store for claim on %s on event %s",
                            claim.getReferenceNumber(), event.getValue()),
                        exception
                    );
                }
            }
        }
    }

    private boolean eventHasBeenPerformedOnClaim(CaseEvent event, Claim claim) {
        switch (event) {
            case SUBMIT_PRE_PAYMENT:
                return true;
            case SUBMIT_POST_PAYMENT:
                return claim.getCreatedAt() != null;
            case LINK_DEFENDANT:
                return StringUtils.isNoneBlank(claim.getDefendantId());
            case MORE_TIME_REQUESTED_ONLINE:
                return claim.isMoreTimeRequested();
            case FULL_ADMISSION:
                return claim.getRespondedAt() != null
                    && claim.getResponse().isPresent()
                    && claim.getResponse().get().getResponseType() == ResponseType.FULL_ADMISSION;
            case PART_ADMISSION:
                return claim.getRespondedAt() != null
                    && claim.getResponse().isPresent()
                    && claim.getResponse().get().getResponseType() == ResponseType.PART_ADMISSION;
            case DISPUTE:
                return claim.getRespondedAt() != null
                    && claim.getResponse().isPresent()
                    && claim.getResponse().get().getResponseType() == ResponseType.FULL_DEFENCE
                    && ((FullDefenceResponse) claim.getResponse().get()).getDefenceType() == DefenceType.DISPUTE;
            case ALREADY_PAID:
                return claim.getRespondedAt() != null
                    && claim.getResponse().isPresent()
                    && claim.getResponse().get().getResponseType() == ResponseType.FULL_DEFENCE
                    && ((FullDefenceResponse) claim.getResponse().get()).getDefenceType() == DefenceType.ALREADY_PAID;
            case DEFAULT_CCJ_REQUESTED:
                return claim.getCountyCourtJudgmentRequestedAt() != null
                    && claim.getCountyCourtJudgment() != null
                    && claim.getCountyCourtJudgment().getCcjType() == CountyCourtJudgmentType.DEFAULT;
            case CCJ_BY_ADMISSION:
                return claim.getCountyCourtJudgmentRequestedAt() != null
                    && claim.getCountyCourtJudgment() != null
                    && claim.getCountyCourtJudgment().getCcjType() == CountyCourtJudgmentType.ADMISSIONS;
            case CCJ_BY_DETERMINATION:
                return claim.getCountyCourtJudgmentRequestedAt() != null
                    && claim.getCountyCourtJudgment() != null
                    && claim.getCountyCourtJudgment().getCcjType() == CountyCourtJudgmentType.DETERMINATION;
            case DIRECTIONS_QUESTIONNAIRE_DEADLINE:

            default:
                return false;
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

    private Optional<Long> search(User user, Map<String, String> searchString) {

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
