package uk.gov.hmcts.cmc.claimstore.repositories;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.ClaimMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.Optional;

@RegisterMapper(ClaimMapper.class)
@SuppressWarnings("squid:S1214") // Pointless to create class for string statement
public interface ClaimSearchRepository {
    @SuppressWarnings("squid:S1214") // Pointless to create class for this
        String SELECT_FROM_STATEMENT = "SELECT * FROM claim";

    @SuppressWarnings("squid:S1214") // Pointless to create class for this
        String ORDER_BY_ID_DESCENDING = " ORDER BY claim.id DESC";

    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.submitter_id = :submitterId" + ORDER_BY_ID_DESCENDING)
    List<Claim> getBySubmitterId(@Bind("submitterId") String submitterId);


    @SingleValueResult
    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.external_id = :externalId")
    Optional<Claim> getClaimByExternalId(@Bind("externalId") String externalId);

    @SingleValueResult
    @SqlQuery(SELECT_FROM_STATEMENT + " WHERE claim.reference_number = :claimReferenceNumber "
        + "AND claim.submitter_id = :submitterId")
    Optional<Claim> getByClaimReferenceAndSubmitter(@Bind("claimReferenceNumber") String claimReferenceNumber,
                                                    @Bind("submitterId") String submitterId);

}
