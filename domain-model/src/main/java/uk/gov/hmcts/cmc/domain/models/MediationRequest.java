package uk.gov.hmcts.cmc.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@Getter
public class MediationRequest {

    @NotEmpty
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate reportDate;

    @NotEmpty
    private String recipientEmail;

}
