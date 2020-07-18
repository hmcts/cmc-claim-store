package uk.gov.hmcts.cmc.domain.models.party;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.cmc.domain.constraints.AgeRangeValidator;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
public class Individual extends Party {

    @JsonUnwrapped
    @AgeRangeValidator
    private final LocalDate dateOfBirth;

    @Builder
    public Individual(
        String id,
        String name,
        Address address,
        Address correspondenceAddress,
        String phone,
        String mobilePhone,
        Representative representative,
        LocalDate dateOfBirth
    ) {
        super(id, name, address, correspondenceAddress, phone, mobilePhone, representative);
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

}
