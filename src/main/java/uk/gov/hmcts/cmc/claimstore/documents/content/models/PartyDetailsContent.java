package uk.gov.hmcts.cmc.claimstore.documents.content.models;

import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;

import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public class PartyDetailsContent {

    private final String type;
    private final String fullName;
    private final Boolean nameAmended;
    private final String businessName;
    private final String contactPerson;
    private final Address address;
    private final Address correspondenceAddress;
    private final Boolean addressAmended;
    private final String dateOfBirth;
    private final String email;

    public PartyDetailsContent(
        final TheirDetails providedByClaimant,
        final Party party,
        final String email
    ) {
        this.nameAmended  = !providedByClaimant.getName().equals(party.getName());
        this.addressAmended = !providedByClaimant.getAddress().equals(party.getAddress());
        this.type = PartyUtils.getType(party);
        this.fullName = nameAmended ? party.getName() : providedByClaimant.getName();
        this.businessName = PartyUtils.getBusinessName(party).orElse(null);
        this.contactPerson = PartyUtils.getContactPerson(party).orElse(null);
        this.address = addressAmended ? party.getAddress() : providedByClaimant.getAddress();
        this.correspondenceAddress = party.getCorrespondenceAddress().orElse(null);
        this.dateOfBirth = defendantDateOfBirth(party).orElse(null);
        this.email = email;
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

    public String getBusinessName() {
        return businessName;
    }

    public String getContactPerson() {
        return contactPerson;
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
