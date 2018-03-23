package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.utils.DatesProvider;

import java.time.LocalDate;

public class SampleInterestDate {

    private InterestDate.InterestDateType type = InterestDate.InterestDateType.CUSTOM;
    private LocalDate date = DatesProvider.INTEREST_DATE;
    private String reason = "I want to claim from this date because that's when that happened";
    private InterestDate.InterestEndDateType endDateType = InterestDate.InterestEndDateType.SETTLED_OR_JUDGMENT;

    public static SampleInterestDate builder() {
        return new SampleInterestDate();
    }

    public static InterestDate validDefaults() {
        return builder().build();
    }

    public static InterestDate submission() {
        return builder()
            .withType(InterestDate.InterestDateType.SUBMISSION)
            .withDate(null)
            .withReason(null)
            .build();
    }

    public static InterestDate submissionToSubmission() {
        return builder()
            .withType(InterestDate.InterestDateType.SUBMISSION)
            .withEndDateType(InterestDate.InterestEndDateType.SUBMISSION)
            .withDate(null)
            .withReason(null)
            .build();
    }

    public static InterestDate submissionToSettledOrJudgement() {
        return builder()
            .withType(InterestDate.InterestDateType.SUBMISSION)
            .withEndDateType(InterestDate.InterestEndDateType.SETTLED_OR_JUDGMENT)
            .withDate(null)
            .withReason(null)
            .build();
    }

    public SampleInterestDate withType(InterestDate.InterestDateType type) {
        this.type = type;
        return this;
    }

    public SampleInterestDate withDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public SampleInterestDate withReason(String reason) {
        this.reason = reason;
        return this;
    }

    public SampleInterestDate withEndDateType(InterestDate.InterestEndDateType endDateType) {
        this.endDateType = endDateType;
        return this;
    }

    public InterestDate build() {
        return new InterestDate(type, date, reason, endDateType);
    }

}
