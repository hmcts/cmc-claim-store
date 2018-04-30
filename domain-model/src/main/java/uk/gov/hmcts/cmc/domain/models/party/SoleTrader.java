package uk.gov.hmcts.cmc.domain.models.party;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;

import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SoleTrader extends Party implements TitledParty {

    @Size(max = 35, message = "must be at most {max} characters")
    private final String title;

    private final String businessName;

    public SoleTrader(
        String name,
        Address address,
        Address correspondenceAddress,
        String phoneNumber,
        Representative representative,
        String title,
        String businessName
    ) {
        super(name, address, correspondenceAddress, phoneNumber, representative);
        this.title = title;
        this.businessName = businessName;
    }

    @Override
    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    public Optional<String> getBusinessName() {
        return Optional.ofNullable(businessName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        SoleTrader other = (SoleTrader) obj;

        return super.equals(other)
            && Objects.equals(title, other.title)
            && Objects.equals(businessName, other.businessName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), title, businessName);
    }

}
