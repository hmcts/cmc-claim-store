package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.VulnerabilityQuestions;

import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

public class SampleVQ {

    public static final VulnerabilityQuestions defaultVQ = VulnerabilityQuestions.builder()
        .vulnerabilityQuestions(YES)
        .vulnerabilityDetails("Some details")
        .build();

    private SampleVQ() {
        // Do Nothing constructor
    }

}
