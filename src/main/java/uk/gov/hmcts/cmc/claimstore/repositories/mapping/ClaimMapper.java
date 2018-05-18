package uk.gov.hmcts.cmc.claimstore.repositories.mapping;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.sql.ResultSet;
import java.sql.SQLException;

import static uk.gov.hmcts.cmc.claimstore.repositories.mapping.MappingUtils.toLocalDateTimeFromUTC;
import static uk.gov.hmcts.cmc.claimstore.repositories.mapping.MappingUtils.toNullableLocalDateTimeFromUTC;

public class ClaimMapper implements ResultSetMapper<Claim> {
    private final JsonMapper jsonMapper = JsonMapperFactory.create();

    @Override
    public Claim map(int index, ResultSet result, StatementContext ctx) throws SQLException {

        return new Claim(
            result.getLong("id"),
            result.getString("submitter_id"),
            result.getString("letter_holder_id"),
            result.getString("defendant_id"),
            result.getString("external_id"),
            result.getString("reference_number"),
            toClaimData(result.getString("claim")),
            toLocalDateTimeFromUTC(result.getTimestamp("created_at")),
            result.getTimestamp("issued_on").toLocalDateTime().toLocalDate(),
            result.getTimestamp("response_deadline").toLocalDateTime().toLocalDate(),
            result.getBoolean("more_time_requested"),
            result.getString("submitter_email"),
            toNullableLocalDateTimeFromUTC(result.getTimestamp("responded_at")),
            toNullableResponseData(result.getString("response")),
            result.getString("defendant_email"),
            toNullableCountyCourtJudgment(result.getString("county_court_judgment")),
            toNullableLocalDateTimeFromUTC(result.getTimestamp("county_court_judgment_requested_at")),
            toNullableSettlement(result.getString("settlement")),
            toNullableLocalDateTimeFromUTC(result.getTimestamp("settlement_reached_at")),
            result.getString("sealed_claim_document_management_self_path")
        );
    }

    private ClaimData toClaimData(String input) {
        return jsonMapper.fromJson(input, ClaimData.class);
    }

    private Response toNullableResponseData(String input) {
        return toNullableEntity(input, Response.class);
    }

    private CountyCourtJudgment toNullableCountyCourtJudgment(String input) {
        return toNullableEntity(input, CountyCourtJudgment.class);
    }

    private Settlement toNullableSettlement(String input) {
        return toNullableEntity(input, Settlement.class);
    }

    private <T> T toNullableEntity(String input, Class<T> entityClass) {
        if (input == null) {
            return null;
        } else {
            return jsonMapper.fromJson(input, entityClass);
        }
    }

}
