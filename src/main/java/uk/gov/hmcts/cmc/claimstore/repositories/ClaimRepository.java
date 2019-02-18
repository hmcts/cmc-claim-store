package uk.gov.hmcts.cmc.claimstore.repositories;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.ClaimMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RegisterMapper(ClaimMapper.class)
@SuppressWarnings("squid:S1214") // Pointless to create class for string statement
public interface ClaimRepository {

    @SuppressWarnings("squid:S1214") // Pointless to create class for this
        String SELECT_FROM_STATEMENT = "SELECT * FROM claim";

    @SuppressWarnings("squid:S1214") // Pointless to create class for this
        String ORDER_BY_ID_DESCENDING = " ORDER BY claim.id DESC";

    @SqlQuery(SELECT_FROM_STATEMENT + ORDER_BY_ID_DESCENDING)
    List<Claim> findAll();

    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.submitter_id = :submitterId" + ORDER_BY_ID_DESCENDING)
    List<Claim> getBySubmitterId(@Bind("submitterId") String submitterId);

    @SingleValueResult
    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.letter_holder_id = :letterHolderId")
    Optional<Claim> getByLetterHolderId(@Bind("letterHolderId") String letterHolderId);

    @SingleValueResult
    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.external_id = :externalId")
    Optional<Claim> getClaimByExternalId(@Bind("externalId") String externalId);

    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.defendant_id = :defendantId" + ORDER_BY_ID_DESCENDING)
    List<Claim> getByDefendantId(@Bind("defendantId") String defendantId);

    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.submitter_email = :submitterEmail")
    List<Claim> getBySubmitterEmail(@Bind("submitterEmail") String submitterEmail);

    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.defendant_email = :defendantEmail")
    List<Claim> getByDefendantEmail(@Bind("defendantEmail") String defendantEmail);

    @SingleValueResult
    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.reference_number = :claimReferenceNumber")
    Optional<Claim> getByClaimReferenceNumber(@Bind("claimReferenceNumber") String claimReferenceNumber);

    @SingleValueResult
    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.reference_number = :claimReferenceNumber "
        + "AND claim.submitter_id = :submitterId")
    Optional<Claim> getByClaimReferenceAndSubmitter(@Bind("claimReferenceNumber") String claimReferenceNumber,
                                                    @Bind("submitterId") String submitterId);

    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.submitter_id = :submitterId "
        + " AND claim.claim ->>'externalReferenceNumber' = :externalReference" + ORDER_BY_ID_DESCENDING)
    List<Claim> getByExternalReference(@Bind("externalReference") String externalReference,
                                       @Bind("submitterId") String submitterId);

    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim->'payment'->>'reference' = :payReference")
    List<Claim> getByPaymentReference(@Bind("payReference") String payReference);

    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.is_migrated = false")
    List<Claim> getAllNotMigratedClaims();

    @SingleValueResult
    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.id = :id")
    Optional<Claim> getById(@Bind("id") Long id);

    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO claim ( "
        + "submitter_id, "
        + "claim, "
        + "issued_on, "
        + "response_deadline, "
        + "external_id, "
        + "submitter_email, "
        + "reference_number, "
        + "features"
        + ") "
        + "VALUES ("
        + ":submitterId, "
        + ":claim::JSONB, "
        + ":issuedOn, "
        + ":responseDeadline, "
        + ":externalId, "
        + ":submitterEmail, "
        + "next_legal_rep_reference_number(), "
        + ":features::JSONB"
        + ")")
    Long saveRepresented(
        @Bind("claim") String claim,
        @Bind("submitterId") String submitterId,
        @Bind("issuedOn") LocalDate issuedOn,
        @Bind("responseDeadline") LocalDate responseDeadline,
        @Bind("externalId") String externalId,
        @Bind("submitterEmail") String submitterEmail,
        @Bind("features") String features
    );

    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO claim ( "
        + "submitter_id, "
        + "claim, "
        + "letter_holder_id, "
        + "issued_on, "
        + "response_deadline, "
        + "external_id, "
        + "submitter_email, "
        + "reference_number, "
        + "features"
        + ") "
        + "VALUES ("
        + ":submitterId, "
        + ":claim::JSONB, "
        + ":letterHolderId, "
        + ":issuedOn, "
        + ":responseDeadline, "
        + ":externalId, "
        + ":submitterEmail, "
        + "next_reference_number(), "
        + ":features::JSONB"
        + ")")
    Long saveSubmittedByClaimant(
        @Bind("claim") String claim,
        @Bind("submitterId") String submitterId,
        @Bind("letterHolderId") String letterHolderId,
        @Bind("issuedOn") LocalDate issuedOn,
        @Bind("responseDeadline") LocalDate responseDeadline,
        @Bind("externalId") String externalId,
        @Bind("submitterEmail") String submitterEmail,
        @Bind("features") String features
    );

    @SqlUpdate(
        "UPDATE claim SET letter_holder_id = :letterHolderId WHERE id = :claimId"
    )
    Integer linkLetterHolder(
        @Bind("claimId") Long claimId,
        @Bind("letterHolderId") String letterHolderId
    );

    @SqlUpdate(
        "UPDATE claim SET claim_document_store = :claimDocumentStore::JSONB"
            + " WHERE id = :claimId"
    )
    Integer linkClaimToDocument(
        @Bind("claimId") Long claimId,
        @Bind("claimDocumentStore") String claimDocumentStore
    );

    @SqlUpdate(
        "UPDATE claim SET more_time_requested = TRUE, response_deadline = :responseDeadline "
            + "WHERE external_id = :externalId AND more_time_requested = FALSE"
    )
    void requestMoreTime(
        @Bind("externalId") String externalId,
        @Bind("responseDeadline") LocalDate responseDeadline
    );

    @SqlUpdate(
        "UPDATE CLAIM SET "
            + "response = :response::JSONB, "
            + "defendant_email = :defendantEmail, "
            + "responded_at = now() AT TIME ZONE 'utc' "
            + "WHERE external_id = :externalId"
    )
    void saveDefendantResponse(
        @Bind("externalId") String externalId,
        @Bind("defendantEmail") String defendantEmail,
        @Bind("response") String response
    );

    @SqlUpdate(
        "UPDATE CLAIM SET "
            + "claimant_response = :response::JSONB, "
            + "claimant_responded_at = now() AT TIME ZONE 'utc' "
            + "WHERE external_id = :externalId"
    )
    void saveClaimantResponse(
        @Bind("externalId") String externalId,
        @Bind("response") String response
    );

    @SqlUpdate(
        "UPDATE claim SET money_received_on = :moneyReceivedOn "
            + "WHERE external_id = :externalId"
    )
    void updateMoneyReceivedOn(
        @Bind("externalId") String externalId,
        @Bind("moneyReceivedOn") LocalDate moneyReceivedOn
    );

    @SqlUpdate("UPDATE claim SET "
        + " directions_questionnaire_deadline = :dqDeadline"
        + " WHERE external_id = :externalId")
    void updateDirectionsQuestionnaireDeadline(
        @Bind("externalId") String externalId,
        @Bind("dqDeadline") LocalDate dqDeadline
    );

    @SqlUpdate("UPDATE claim SET "
        + " county_court_judgment = :countyCourtJudgmentData::JSONB,"
        + " county_court_judgment_requested_at = :ccjRequestedAt"
        + " WHERE external_id = :externalId")
    void saveCountyCourtJudgment(
        @Bind("externalId") String externalId,
        @Bind("countyCourtJudgmentData") String countyCourtJudgmentData,
        @Bind("ccjRequestedAt") LocalDateTime ccjRequestedAt
    );

    @SqlUpdate("UPDATE claim SET "
        + " re_determination = :reDetermination::JSONB,"
        + " re_determination_requested_at = now() AT TIME ZONE 'utc' "
        + " WHERE external_id = :externalId")
    void saveReDetermination(
        @Bind("externalId") String externalId,
        @Bind("reDetermination") String reDetermination);

    @SqlUpdate(
        "UPDATE claim SET "
            + "defendant_id = :defendantId,"
            + "defendant_email = :defendantEmail "
            + "WHERE letter_holder_id = :letterHolderId AND defendant_id is null"
    )
    Integer linkDefendant(
        @Bind("letterHolderId") String letterHolderId,
        @Bind("defendantId") String defendantId,
        @Bind("defendantEmail") String defendantEmail
    );
}
