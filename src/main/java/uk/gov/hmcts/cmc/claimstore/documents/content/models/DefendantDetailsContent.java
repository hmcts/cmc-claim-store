package uk.gov.hmcts.cmc.claimstore.documents.content.models;

import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.DefendantResponse;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.claimstore.models.party.Individual;
import uk.gov.hmcts.cmc.claimstore.models.party.Party;
import uk.gov.hmcts.cmc.claimstore.utils.PartyTypeContentProvider;

import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public class DefendantDetailsContent {

    private final String type;
    private final String fullName;
    private final Boolean nameAmended;
    private final Address address;
    private final Address correspondenceAddress;
    private final Boolean addressAmended;
    private final String dateOfBirth;
    private final String email;
    private final String businessName;
    private final String contactPerson;

    public DefendantDetailsContent(
        final TheirDetails providedByClaimant,
        final DefendantResponse defendantResponse,
        final Party defendant
    ) {
        final boolean nameAmended = !providedByClaimant.getName().equals(defendant.getName());
        final boolean addressAmended = !providedByClaimant.getAddress().equals(defendant.getAddress());
        this.type = PartyTypeContentProvider.getType(providedByClaimant);
        this.fullName = nameAmended ? defendant.getName() : providedByClaimant.getName();
        this.nameAmended = nameAmended;
        this.address = addressAmended ? defendant.getAddress() : providedByClaimant.getAddress();
        this.correspondenceAddress = correspondenceAddress(defendantResponse);
        this.addressAmended = addressAmended;
        this.dateOfBirth = defendantDateOfBirth(defendant).orElse(null);
        this.email = defendantResponse.getDefendantEmail();
        this.businessName = PartyTypeContentProvider.getDefendantBusinessName(providedByClaimant).orElse(null);
        this.contactPerson = PartyTypeContentProvider.getDefendantContactPerson(providedByClaimant).orElse(null);
    }

    private Address correspondenceAddress(final DefendantResponse defendantResponse) {
        return defendantResponse.getResponse().getDefendant().getCorrespondenceAddress().orElse(null);
    }

    private Optional<String> defendantDateOfBirth(final Party party) {
        if (party instanceof Individual) {
            return Optional.of(formatDate(((Individual) party).getDateOfBirth()));
        }
        return Optional.empty();
    }

    public String getType() {
        return type;
    }

    public String getFullName() {
        return fullName;
    }

    public Boolean getNameAmended() {
        return nameAmended;
    }

    public Address getAddress() {
        return address;
    }

    public Address getCorrespondenceAddress() {
        return correspondenceAddress;
    }

    public Boolean getAddressAmended() {
        return addressAmended;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }
}
