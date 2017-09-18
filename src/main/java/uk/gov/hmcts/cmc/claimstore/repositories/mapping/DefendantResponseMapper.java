package uk.gov.hmcts.cmc.claimstore.repositories.mapping;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.models.DefendantResponse;
import uk.gov.hmcts.cmc.claimstore.models.ResponseData;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import static uk.gov.hmcts.cmc.claimstore.repositories.mapping.MappingUtils.toLocalDateTimeFromUTC;

@Component
public class DefendantResponseMapper implements ResultSetMapper<DefendantResponse> {
    private final JsonMapper jsonMapper = JsonMapperFactory.create();

    @Override
    public DefendantResponse map(int index, ResultSet result, StatementContext ctx) throws SQLException {
        return new DefendantResponse(
            result.getLong("id"),
            result.getLong("claim_id"),
            result.getLong("defendant_id"),
            result.getString("defendant_email"),
            toResponseData(result.getString("response")),
            toLocalDateTimeFromUTC(result.getTimestamp("responded_at"))
        );
    }

    private ResponseData toResponseData(final String input) {
        return jsonMapper.fromJson(input, ResponseData.class);
    }
}
