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

    @SingleValueResult
    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.reference_number = :claimReferenceNumber")
    Optional<Claim> getByClaimReferenceNumberAnonymous(@Bind("claimReferenceNumber") String claimReferenceNumber);

    @SingleValueResult
    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.reference_number = :claimReferenceNumber "
        + "AND claim.submitter_id = :submitterId")
    Optional<Claim> getByClaimReferenceAndSubmitter(@Bind("claimReferenceNumber") String claimReferenceNumber,
                                                    @Bind("submitterId") String submitterId);

    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.submitter_id = :submitterId "
        + " AND claim.claim ->>'externalReferenceNumber' = :externalReference" + ORDER_BY_ID_DESCENDING)
    List<Claim> getByExternalReference(@Bind("externalReference") String externalReference,
                                       @Bind("submitterId") String submitterId);

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
        + "reference_number"
        + ") "
        + "VALUES ("
        + ":submitterId, "
        + ":claim::JSONB, "
        + ":issuedOn, "
        + ":responseDeadline, "
        + ":externalId, "
        + ":submitterEmail, "
        + "next_legal_rep_reference_number()"
        + ")")
    Long saveRepresented(
        @Bind("claim") String claim,
        @Bind("submitterId") String submitterId,
        @Bind("issuedOn") LocalDate issuedOn,
        @Bind("responseDeadline") LocalDate responseDeadline,
        @Bind("externalId") String externalId,
        @Bind("submitterEmail") String submitterEmail
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
        + "reference_number"
        + ") "
        + "VALUES ("
        + ":submitterId, "
        + ":claim::JSONB, "
        + ":letterHolderId, "
        + ":issuedOn, "
        + ":responseDeadline, "
        + ":externalId, "
        + ":submitterEmail, "
        + "next_reference_number()"
        + ")")
    Long saveSubmittedByClaimant(
        @Bind("claim") String claim,
        @Bind("submitterId") String submitterId,
        @Bind("letterHolderId") String letterHolderId,
        @Bind("issuedOn") LocalDate issuedOn,
        @Bind("responseDeadline") LocalDate responseDeadline,
        @Bind("externalId") String externalId,
        @Bind("submitterEmail") String submitterEmail
    );

    @SqlUpdate(
        "UPDATE claim SET letter_holder_id = :letterHolderId WHERE id = :claimId"
    )
    Integer linkLetterHolder(
        @Bind("claimId") Long claimId,
        @Bind("letterHolderId") String letterHolderId
    );

    @SqlUpdate(
        "UPDATE claim SET sealed_claim_document_management_self_path = :documentSelfPath"
            + " WHERE id = :claimId"
    )
    Integer linkSealedClaimDocument(
        @Bind("claimId") Long claimId,
        @Bind("documentSelfPath") String documentSelfPath
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

    @SqlUpdate("UPDATE claim SET "
        + " county_court_judgment = :countyCourtJudgmentData::JSONB,"
        + " county_court_judgment_requested_at = now() at time zone 'utc'"
        + " WHERE external_id = :externalId")
    void saveCountyCourtJudgment(
        @Bind("externalId") String externalId,
        @Bind("countyCourtJudgmentData") String countyCourtJudgmentData
    );

    @SqlUpdate(
        "UPDATE claim SET defendant_id = :defendantId WHERE letter_holder_id = :letterHolderId AND defendant_id is null"
    )
    Integer linkDefendant(
        @Bind("letterHolderId") String letterHolderId,
        @Bind("defendantId") String defendantId
    );
}
