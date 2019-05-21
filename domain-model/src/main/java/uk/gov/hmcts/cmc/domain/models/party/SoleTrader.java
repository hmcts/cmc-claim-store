package uk.gov.hmcts.cmc.domain.models.party;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.cmc.domain.constraints.PartySplitName;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;

import java.util.Optional;
import javax.validation.constraints.Size;

@PartySplitName
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
public class SoleTrader extends Party implements TitledParty, SplitNamedParty {

    @Size(max = 35, message = "must be at most {max} characters")
    private final String title;

    @Size(max = 255, message = "must be at most {max} characters")
    private final String firstName;

    @Size(max = 255, message = "must be at most {max} characters")
    private final String lastName;

    private final String businessName;

    @Builder
    public SoleTrader(
        String id,
        String name,
        Address address,
        Address correspondenceAddress,
        String mobilePhone,
        Representative representative,
        String title,
        String firstName,
        String lastName,
        String businessName
    ) {
        super(id, name, address, correspondenceAddress, mobilePhone, representative);
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.businessName = businessName;
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

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    public Optional<String> getBusinessName() {
        return Optional.ofNullable(businessName);
    }

}
