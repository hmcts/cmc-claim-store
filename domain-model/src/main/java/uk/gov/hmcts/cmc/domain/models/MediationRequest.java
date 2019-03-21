package uk.gov.hmcts.cmc.domain.models;

import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;

public class MediationRequest {

    @NotEmpty
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate mediationGenerateDate;

    @NotEmpty
    String mediationServiceEmail;

    public MediationRequest(
        LocalDate mediationGenerateDate,
        String mediationServiceEmail
    ) {
        this.mediationGenerateDate = mediationGenerateDate;
        this.mediationServiceEmail = mediationServiceEmail;
    }

    public LocalDate getMediationGenerateDate() {
        return mediationGenerateDate;
    }

    public String getMediationServiceEmail() {
        return mediationServiceEmail;
    }
}
