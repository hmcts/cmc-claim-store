package uk.gov.hmcts.cmc.rpa.mapper;

import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDateTime;

public class BaseResponseJsonMapper {

    protected static final String DEFENDANT_EMAIL = "j.smith@example.com";

    protected SampleClaim withCommonDefEmailAndRespondedAt() {
        return SampleClaim.builder()
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withRespondedAt(LocalDateTime.of(2018, 4, 26, 1, 1));
    }
}
