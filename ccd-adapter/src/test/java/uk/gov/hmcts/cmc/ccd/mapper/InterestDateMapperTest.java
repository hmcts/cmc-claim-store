package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestEndDateType;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.cmc.domain.models.InterestDate.InterestEndDateType.SUBMISSION;

public class InterestDateMapperTest {

    private final InterestDateMapper mapper = new InterestDateMapper();

    @Test
    public void shouldMapToCCDInterestDate() {

        InterestDate interestDate = SampleInterestDate.builder()
            .withEndDateType(SUBMISSION)
            .withType(null)
            .withReason(null)
            .withDate(null)
            .build();
        CCDCase.CCDCaseBuilder caseBuilder = CCDCase.builder();

        mapper.to(interestDate, caseBuilder);

        CCDInterestEndDateType interestEndDateType = caseBuilder.build().getInterestEndDateType();
        assertEquals(interestEndDateType.name(), SUBMISSION.name());
    }

    @Test
    public void shouldAddInterestDateAttributesWhenInterestDateIsNull() {

        CCDCase.CCDCaseBuilder caseBuilder = CCDCase.builder();

        mapper.to(null, caseBuilder);

        CCDCase ccdCase = caseBuilder.build();
        assertNull(ccdCase.getInterestEndDateType());
        assertNull(ccdCase.getInterestClaimStartDate());
        assertNull(ccdCase.getInterestDateType());
        assertNull(ccdCase.getInterestStartDateReason());
    }

    @Test
    public void shouldMapFromCCDCase() {
        CCDCase ccdCase = CCDCase.builder()
            .interestEndDateType(CCDInterestEndDateType.SUBMISSION)
            .build();

        InterestDate interestDate = mapper.from(ccdCase);

        assertEquals(interestDate.getEndDateType().name(), ccdCase.getInterestEndDateType().name());
    }

    @Test
    public void shouldReturnNullWhenAllAttributesAreNull() {
        CCDCase ccdCase = CCDCase.builder()
            .interestEndDateType(null)
            .interestClaimStartDate(null)
            .interestDateType(null)
            .interestReason("")
            .build();

        assertNull(mapper.from(ccdCase));
    }
}
