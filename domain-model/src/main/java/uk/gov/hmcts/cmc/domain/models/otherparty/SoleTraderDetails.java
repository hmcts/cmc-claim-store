package uk.gov.hmcts.cmc.domain.models.otherparty;

import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.party.TitledParty;

import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.Size;

public class SoleTraderDetails extends TheirDetails implements TitledParty {

    @Size(max = 35, message = "must be at most {max} characters")
    private final String title;

    @Size(max = 35, message = "may not be longer than {max} characters")
    private final String businessName;

    public SoleTraderDetails(
        String name,
        Address address,
        String email,
        Representative representative,
        Address serviceAddress,
        String title,
        String businessName
    ) {
        super(name, address, email, representative, serviceAddress);
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

        SoleTraderDetails that = (SoleTraderDetails) obj;

        return super.equals(that)
            && Objects.equals(this.title, that.title)
            && Objects.equals(this.businessName, that.businessName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.title, this.businessName);
    }

}
