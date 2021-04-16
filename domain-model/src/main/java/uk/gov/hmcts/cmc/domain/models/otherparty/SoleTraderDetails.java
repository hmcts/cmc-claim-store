package uk.gov.hmcts.cmc.domain.models.otherparty;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.cmc.domain.constraints.SplitName;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.party.SplitNamedParty;
import uk.gov.hmcts.cmc.domain.models.party.TitledParty;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;

import java.util.Optional;
import javax.validation.constraints.Size;

@SplitName
@EqualsAndHashCode(callSuper = true)
public class SoleTraderDetails extends TheirDetails implements TitledParty, SplitNamedParty {

    @Size(max = 35, message = "must be at most {max} characters")
    private final String title;

    // todo ROC-5160 remove @SplitName and put @NotBlank when frontend is merged
    @Size(max = 255, message = "must be at most {max} characters")
    private final String firstName;

    // todo ROC-5160 remove @SplitName and put @NotBlank when frontend is merged
    @Size(max = 255, message = "must be at most {max} characters")
    private final String lastName;

    @Size(max = 35, message = "may not be longer than {max} characters")
    private final String businessName;

    @Builder
    public SoleTraderDetails(
        String id,
        String name,
        String firstName,
        String lastName,
        Address address,
        String email,
        Representative representative,
        Address serviceAddress,
        Address claimantProvidedAddress,
        String title,
        String businessName,
        String phoneNumber
    ) {
        super(id, name, address, email, representative, serviceAddress, claimantProvidedAddress, phoneNumber);
        this.title = title;
        this.businessName = businessName;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String getName() {
        if (StringUtils.isNotBlank(firstName) && StringUtils.isNotBlank(lastName)) {
            return PartyUtils.fullNameFrom(title, firstName, lastName);
        }
        return super.getName();
    }

    @Override
    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    public Optional<String> getBusinessName() {
        return Optional.ofNullable(businessName);
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }
}
