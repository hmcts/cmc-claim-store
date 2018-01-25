package uk.gov.hmcts.cmc.claimstore.repositories;

import org.skife.jdbi.v2.sqlobject.Bind;
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

    @SingleValueResult
    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.letter_holder_id = :letterHolderId")
    Optional<Claim> getByLetterHolderId(@Bind("letterHolderId") String letterHolderId);

    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.defendant_id = :defendantId" + ORDER_BY_ID_DESCENDING)
    List<Claim> getByDefendantId(@Bind("defendantId") String defendantId);

    @SingleValueResult
    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.reference_number = :claimReferenceNumber")
    Optional<Claim> getByClaimReferenceNumber(@Bind("claimReferenceNumber") String claimReferenceNumber);

    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.submitter_id = :submitterId "
        + " AND claim.claim ->>'externalReferenceNumber' = :externalReference" + ORDER_BY_ID_DESCENDING)
    List<Claim> getByExternalReference(@Bind("externalReference") String externalReference,
                                       @Bind("submitterId") String submitterId);

    @SingleValueResult
    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.id = :id")
    Optional<Claim> getById(@Bind("id") Long id);

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
            + "WHERE id = :claimId AND more_time_requested = FALSE"
    )
    void requestMoreTime(
        @Bind("claimId") Long claimId,
        @Bind("responseDeadline") LocalDate responseDeadline
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
        @Bind("claimId") Long claimId,
        @Bind("defendantId") String defendantId,
        @Bind("defendantEmail") String defendantEmail,
        @Bind("response") String response
    );

    @SqlUpdate("UPDATE claim SET "
        + " county_court_judgment = :countyCourtJudgmentData::JSONB,"
        + " county_court_judgment_requested_at = now() at time zone 'utc'"
        + "WHERE"
        + " id = :claimId")
    void saveCountyCourtJudgment(
        @Bind("claimId") long claimId,
        @Bind("countyCourtJudgmentData") String countyCourtJudgmentData
    );
}
