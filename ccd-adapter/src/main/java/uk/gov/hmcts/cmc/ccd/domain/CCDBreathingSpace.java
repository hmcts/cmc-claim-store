package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
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
