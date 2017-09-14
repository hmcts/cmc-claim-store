package uk.gov.hmcts.cmc.claimstore.repositories.mapping;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.hmcts.cmc.claimstore.models.DefaultJudgment;
import uk.gov.hmcts.cmc.claimstore.utils.LocalDateTimeFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class DefaultJudgmentMapper implements ResultSetMapper<DefaultJudgment> {

    @Override
    public DefaultJudgment map(int index, ResultSet result, StatementContext ctx) throws SQLException {

        return new DefaultJudgment(
            result.getLong("id"),
            result.getLong("claim_id"),
            result.getLong("claimant_id"),
            result.getString("external_id"),
            result.getString("data"),
            toLocalDateTimeFromUTC(result.getTimestamp("created_at"))
        );
    }

    private LocalDateTime toLocalDateTimeFromUTC(Timestamp input) {
        return LocalDateTimeFactory.fromUTC(input.toLocalDateTime());
    }
}
