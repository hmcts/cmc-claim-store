package uk.gov.hmcts.cmc.ccd.migration.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.hmcts.cmc.ccd.migration.config.JacksonConfiguration;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.cmc.ccd.migration.mappers.MappingUtils.toLocalDateTimeFromUTC;
import static uk.gov.hmcts.cmc.ccd.migration.mappers.MappingUtils.toNullableLocalDateFromUTC;
import static uk.gov.hmcts.cmc.ccd.migration.mappers.MappingUtils.toNullableLocalDateTimeFromUTC;

public class ClaimMapper implements ResultSetMapper<Claim> {

    private final JsonMapper jsonMapper = new JsonMapper(
        new JacksonConfiguration().objectMapper()
    );

    @Override
    public Claim map(int index, ResultSet result, StatementContext ctx) throws SQLException {
        LocalDate issuedOn = result.getTimestamp("issued_on").toLocalDateTime().toLocalDate();
        return new Claim(
            result.getLong("id"),
            result.getString("submitter_id"),
            result.getString("letter_holder_id"),
            result.getString("defendant_id"),
            result.getString("external_id"),
            result.getString("reference_number"),
            toClaimData(result.getString("claim")),
            toLocalDateTimeFromUTC(result.getTimestamp("created_at")),
            issuedOn,
            issuedOn.plusDays(5),
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
            toList(result.getString("features")),
            toNullableLocalDateTimeFromUTC(result.getTimestamp("claimant_responded_at")),
            toNullableEntity(result.getString("claimant_response"), ClaimantResponse.class),
            toNullableLocalDateFromUTC(result.getTimestamp("directions_questionnaire_deadline")),
            toNullableLocalDateFromUTC(result.getTimestamp("money_received_on")),
            toNullableEntity(result.getString("re_determination"), ReDetermination.class),
            toNullableLocalDateTimeFromUTC(result.getTimestamp("re_determination_requested_at")),
            toNullableEntity(result.getString("claim_documents"), ClaimDocumentCollection.class),
            toNullableLocalDateFromUTC(result.getTimestamp("claimant_response_deadline")),
            toNullableClaimState(result.getString("state")),
            toNullableEntity(result.getString("submission_operation_indicators"),
                ClaimSubmissionOperationIndicators.class),
            null,
            null, // Not implemented to save review order in claim store database
            null, //not implemented for db
            null,
            null,
            null,
            null,
            null
        );
    }

    private ClaimState toNullableClaimState(String state) {
        return state != null ? ClaimState.valueOf(state) : null;
    }

    private URI mapNullableUri(String uri) {
        return uri != null ? URI.create(uri) : null;
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

    private List<String> toList(String input) {
        return toNullableEntity(input, new TypeReference<List<String>>() {
        });
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

    private <T> T toNullableEntity(String input, TypeReference<T> entityClass) {
        if (input == null) {
            return null;
        } else {
            return jsonMapper.fromJson(input, entityClass);
        }
    }
}
