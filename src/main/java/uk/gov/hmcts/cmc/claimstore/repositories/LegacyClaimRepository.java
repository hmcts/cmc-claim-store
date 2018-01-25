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

/**
 * Holds calls that we have migrated to CCD
 * but have kept the code around until we do the full switch over
 */
@RegisterMapper(ClaimMapper.class)
@SuppressWarnings("squid:S1214") // Pointless to create class for string statement
public interface LegacyClaimRepository {
    @SuppressWarnings("squid:S1214") // Pointless to create class for this
        String SELECT_FROM_STATEMENT = "SELECT * FROM claim";

    @SuppressWarnings("squid:S1214") // Pointless to create class for this
        String ORDER_BY_ID_DESCENDING = " ORDER BY claim.id DESC";

    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.submitter_id = :submitterId" + ORDER_BY_ID_DESCENDING)
    List<Claim> getBySubmitterId(@Bind("submitterId") String submitterId);


    @SingleValueResult
    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.external_id = :externalId")
    Optional<Claim> getByExternalId(@Bind("externalId") String externalId);

    @SingleValueResult
    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.reference_number = :claimReferenceNumber "
        + "AND claim.submitter_id = :submitterId")
    Optional<Claim> getByReferenceAndSubmitter(@Bind("claimReferenceNumber") String claimReferenceNumber,
                                               @Bind("submitterId") String submitterId);

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
        "UPDATE claim SET defendant_id = :defendantId WHERE id = :claimId"
    )
    Integer linkDefendant(
        @Bind("claimId") Long claimId,
        @Bind("defendantId") String defendantId
    );
}
