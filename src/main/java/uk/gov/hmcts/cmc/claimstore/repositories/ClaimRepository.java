package uk.gov.hmcts.cmc.claimstore.repositories;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.ClaimMapper;

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

    @SingleValueResult
    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.id = :id")
    Optional<Claim> getById(@Bind("id") final Long id);

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
        @Bind("claim") final String claim,
        @Bind("submitterId") final String submitterId,
        @Bind("issuedOn") final LocalDate issuedOn,
        @Bind("responseDeadline") final LocalDate responseDeadline,
        @Bind("externalId") final String externalId,
        @Bind("submitterEmail") final String submitterEmail
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
        @Bind("claim") final String claim,
        @Bind("submitterId") final String submitterId,
        @Bind("letterHolderId") final String letterHolderId,
        @Bind("issuedOn") final LocalDate issuedOn,
        @Bind("responseDeadline") final LocalDate responseDeadline,
        @Bind("externalId") final String externalId,
        @Bind("submitterEmail") final String submitterEmail
    );

    @SqlUpdate(
        "UPDATE claim SET letter_holder_id = :letterHolderId WHERE id = :claimId"
    )
    Integer linkLetterHolder(
        @Bind("claimId") final Long claimId,
        @Bind("letterHolderId") final String letterHolderId
    );

    @SqlUpdate(
        "UPDATE claim SET defendant_id = :defendantId WHERE id = :claimId"
    )
    Integer linkDefendant(
        @Bind("claimId") final Long claimId,
        @Bind("defendantId") final String defendantId
    );

    @SqlUpdate(
        "UPDATE claim SET more_time_requested = TRUE, response_deadline = :responseDeadline "
            + "WHERE id = :claimId AND more_time_requested = FALSE"
    )
    void requestMoreTime(
        @Bind("claimId") final Long claimId,
        @Bind("responseDeadline") final LocalDate responseDeadline
    );

    @SqlUpdate(
        "UPDATE CLAIM SET "
            + "response = :response::JSONB, "
            + "defendant_id = :defendantId, "
            + "defendant_email = :defendantEmail, "
            + "responded_at = now() AT TIME ZONE 'utc' "
            + "WHERE id = :claimId"
    )
    void saveDefendantResponse(
        @Bind("claimId") final Long claimId,
        @Bind("defendantId") final String defendantId,
        @Bind("defendantEmail") final String defendantEmail,
        @Bind("response") final String response
    );

    @SqlUpdate("UPDATE claim SET "
        + " county_court_judgment = :countyCourtJudgmentData::JSONB,"
        + " county_court_judgment_requested_at = now() at time zone 'utc'"
        + "WHERE"
        + " id = :claimId")
    void saveCountyCourtJudgment(
        @Bind("claimId") final long claimId,
        @Bind("countyCourtJudgmentData") final String countyCourtJudgmentData
    );
}
