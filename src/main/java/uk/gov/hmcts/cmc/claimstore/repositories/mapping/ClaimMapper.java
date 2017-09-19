package uk.gov.hmcts.cmc.claimstore.repositories.mapping;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.models.ResponseData;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.repositories.mapping.MappingUtils.toLocalDateTimeFromUTC;
import static uk.gov.hmcts.cmc.claimstore.repositories.mapping.MappingUtils.toNullableLocalDateTimeFromUTC;

public class ClaimMapper implements ResultSetMapper<Claim> {
    private final JsonMapper jsonMapper = JsonMapperFactory.create();

    @Override
    public Claim map(int index, ResultSet result, StatementContext ctx) throws SQLException {
        return new Claim(
            result.getLong("id"),
            result.getLong("submitter_id"),
            result.getLong("letter_holder_id"),
            toNullableLong((Integer) result.getObject("defendant_id")),
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
            toCountyCourtJudgment(result.getString("county_court_judgment")),
            toNullableLocalDateTimeFromUTC(result.getTimestamp("county_court_judgment_requested_at"))
        );
    }

    private ClaimData toClaimData(final String input) {
        return jsonMapper.fromJson(input, ClaimData.class);
    }

    private ResponseData toNullableResponseData(final String input) {
        return input != null ? jsonMapper.fromJson(input, ResponseData.class) : null;
    }

    private Long toNullableLong(final Integer input) {
        return input != null ? input.longValue() : null;
    }

    private Map<String, Object> toCountyCourtJudgment(final String input) {
        return input != null ? jsonMapper.fromJson(input, TempCCJClass.class) : null;
    }

    private class TempCCJClass extends HashMap<String, Object> {

    }
}
