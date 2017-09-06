package uk.gov.hmcts.cmc.claimstore.models.otherparty;

import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.Representative;
import uk.gov.hmcts.cmc.claimstore.models.party.TitledParty;

import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.Size;

public class IndividualDetails extends TheirDetails implements TitledParty {

    @Size(max = 35, message = "must be at most {max} characters")
    private final String title;

    public IndividualDetails(String name, Address address, String email, Representative representative, String title) {
        super(name, address, email, representative);
        this.title = title;
    }

    @Override
    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        IndividualDetails that = (IndividualDetails) obj;
        return super.equals(that) && Objects.equals(this.title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.title);
    }

}
