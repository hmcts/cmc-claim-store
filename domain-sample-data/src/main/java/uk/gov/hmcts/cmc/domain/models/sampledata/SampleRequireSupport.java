package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.RequireSupport;

import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

public class SampleRequireSupport {

    public static final RequireSupport defaultRequireSupport = RequireSupport.builder()
        .languageInterpreter("English")
        .signLanguageInterpreter("Need Sign Language")
        .disabledAccess(YES)
        .hearingLoop(YES)
        .build();

    private SampleRequireSupport() {
        // Do Nothing constructor
    }

}
