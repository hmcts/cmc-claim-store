package uk.gov.hmcts.cmc.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;

@AllArgsConstructor
public class MediationRequest {

    @Getter
    @NotEmpty
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate mediationGenerateDate;

    @Getter
    @NotEmpty
    private String mediationServiceEmail;

}
