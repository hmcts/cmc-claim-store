package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.time.LocalDate;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BreathingSpace {

    private String bsReferenceNumber;
    private BreathingSpaceType bsType;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate bsEnteredDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate bsLiftedDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate bsEnteredDateByInsolvencyTeam;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate bsLiftedDateByInsolvencyTeam;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate bsExpectedEndDate;
    private String bsLiftedFlag;

    public BreathingSpace(String bsReferenceNumber,
                          BreathingSpaceType bsType,
                          LocalDate bsEnteredDate,
                          LocalDate bsLiftedDate,
                          LocalDate bsEnteredDateByInsolvencyTeam,
                          LocalDate bsLiftedDateByInsolvencyTeam,
                          LocalDate bsExpectedEndDate,
                          String bsLiftedFlag) {
        this.bsReferenceNumber = bsReferenceNumber;
        this.bsType = bsType;
        this.bsEnteredDate = bsEnteredDate;
        this.bsLiftedDate = bsLiftedDate;
        this.bsEnteredDateByInsolvencyTeam = bsEnteredDateByInsolvencyTeam;
        this.bsLiftedDateByInsolvencyTeam = bsLiftedDateByInsolvencyTeam;
        this.bsExpectedEndDate = bsExpectedEndDate;
        this.bsLiftedFlag = bsLiftedFlag;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
