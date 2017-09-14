package uk.gov.hmcts.cmc.claimstore.repositories.mapping;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.hmcts.cmc.claimstore.models.DefaultJudgment;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.utils.LocalDateTimeFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

public class DefaultJudgmentMapper implements ResultSetMapper<DefaultJudgment> {

    private final JsonMapper jsonMapper = JsonMapperFactory.create();

    @Override
    public DefaultJudgment map(int index, ResultSet result, StatementContext ctx) throws SQLException {

        return new DefaultJudgment(
            result.getLong("id"),
            result.getLong("claim_id"),
            result.getLong("claimant_id"),
            result.getString("external_id"),
            jsonMapper.fromJson(result.getString("data"), Map.class),
            toLocalDateTimeFromUTC(result.getTimestamp("created_at"))
        );
    }

    private LocalDateTime toLocalDateTimeFromUTC(Timestamp input) {
        return LocalDateTimeFactory.fromUTC(input.toLocalDateTime());
    }
}
