package uk.gov.hmcts.cmc.domain.models;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class MediationRequest {

    @NotEmpty
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate reportDate;

    @NotEmpty
    private String recipientEmail;

}
