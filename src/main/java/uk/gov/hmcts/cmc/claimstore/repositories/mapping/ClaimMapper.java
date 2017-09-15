package uk.gov.hmcts.cmc.claimstore.repositories.mapping;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.models.ResponseData;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.utils.LocalDateTimeFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

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
            toResponseData(result.getString("response")),
            result.getString("defendant_email")
        );
    }

    private ClaimData toClaimData(final String input) {
        return jsonMapper.fromJson(input, ClaimData.class);
    }

    private ResponseData toResponseData(final String input) {
        return jsonMapper.fromJson(input, ResponseData.class);
    }

    private Long toNullableLong(final Integer input) {
        return input != null ? input.longValue() : null;
    }

    private LocalDateTime toNullableLocalDateTimeFromUTC(Timestamp input) {
        return input != null ? toLocalDateTimeFromUTC(input) : null;
    }

    private LocalDateTime toLocalDateTimeFromUTC(Timestamp input) {
        return LocalDateTimeFactory.fromUTC(input.toLocalDateTime());
    }
}
