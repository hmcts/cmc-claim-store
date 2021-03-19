package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder(toBuilder = true)
public class CCDBreathingSpace {

    private String bsReferenceNumber;
    private CCDBreathingSpaceType bsType;
    private LocalDate bsEnteredDate;
    private LocalDate bsLiftedDate;
    private LocalDate bsEnteredDateByInsolvencyTeam;
    private LocalDate bsLiftedDateByInsolvencyTeam;
    private LocalDate bsExpectedEndDate;

}
