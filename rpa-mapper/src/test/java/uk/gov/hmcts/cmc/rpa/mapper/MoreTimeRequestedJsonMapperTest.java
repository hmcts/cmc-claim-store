package uk.gov.hmcts.cmc.rpa.mapper;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.config.ModuleConfiguration;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

@SpringBootTest
@ContextConfiguration(classes = ModuleConfiguration.class)
@ExtendWith(SpringExtension.class)
public class MoreTimeRequestedJsonMapperTest {

    private static final String REFERENCE_NUMBER = "000MC001";

    @Autowired
    private MoreTimeRequestedJsonMapper mapper;

    @Test
    public void shouldMapPartyForMoreTimeRequested() throws JSONException {
        Claim claim = SampleClaim.builder()
            .withReferenceNumber(REFERENCE_NUMBER)
            .withResponseDeadline(LocalDate.of(2018, 5, 27))
            .build();
        LocalDateTime responseDeadline = LocalDateTime.now();
        assertEquals(getExpectedJson(), mapper.map(claim, responseDeadline).toString(), STRICT);
    }

    private String getExpectedJson() {
        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", REFERENCE_NUMBER)
            .add("moreTimeRequestedOn", DateFormatter.format(LocalDateTime.now()))
            .add("responseDeadline", "27-May-2018")
            .build().toString();
    }
}
