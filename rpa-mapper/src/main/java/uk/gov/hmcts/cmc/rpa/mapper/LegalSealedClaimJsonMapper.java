package uk.gov.hmcts.cmc.rpa.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import java.util.List;
import java.util.Optional;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonCollectors;

import static java.math.BigInteger.ZERO;
import static javax.json.JsonValue.NULL;
import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractFromSubclass;
import static uk.gov.hmcts.cmc.rpa.mapper.helper.Extractor.extractOptionalFromSubclass;

@Component
@SuppressWarnings({"LineLength"})
public class LegalSealedClaimJsonMapper {

    public JsonObject map(Claim claim) {
        AmountRange amountRange = (AmountRange) claim.getClaimData().getAmount();
        return new NullAwareJsonObjectBuilder()
            .add("claimNumber", claim.getReferenceNumber())
            .add("claimIssueDate", DateFormatter.format(claim.getIssuedOn()))
            .add("claimSubmissionDate", DateFormatter.format(claim.getServiceDate()))
            .add("feeAmountInPennies", claim.getClaimData().getFeeAmountInPennies().orElse(ZERO))
            .add("amountLowerValue", amountRange.getLowerValue().orElse(null))
            .add("amountHigherValue", amountRange.getHigherValue())
            .add("preferredCourt", claim.getClaimData().getPreferredCourt().orElse(null))
            .add("solicitorRef", claim.getClaimData().getExternalReferenceNumber().orElse(null))
            .add("briefDetails", Optional.ofNullable(claim.getClaimData().getReason()).orElse(null))
            .add("defendantRef", NULL)
            .add("Applicants", mapClaimants(claim.getClaimData().getClaimants(), claim.getSubmitterEmail()))
            .add("Respondents", mapDefendants(claim.getClaimData().getDefendants()))
            .build();
    }

    private JsonArray mapClaimants(List<Party> claimants, String submitterEmail) {
        return claimants.stream()
            .map(claimant -> new NullAwareJsonObjectBuilder()
                .add("partyDetail", mapClaimantDetails(claimant, submitterEmail))
                .add("partyName", claimant.getName())
                .add("representativeOrganisationName", claimant.getRepresentative().map(Representative::getOrganisationName).orElse(null))
                .add("representativeOrganisationAddress", claimant.getRepresentative().map(Representative::getOrganisationAddress).map(this::mapAddress).orElse(null))
                .add("representativeOrganisationPhone", claimant.getRepresentative().flatMap(Representative::getOrganisationContactDetails).flatMap(ContactDetails::getPhone).orElse(null))
                .add("representativeOrganisationDxAddress", claimant.getRepresentative().flatMap(Representative::getOrganisationContactDetails).flatMap(ContactDetails::getDxAddress).orElse(null))
                .add("representativeOrganisationEmail", claimant.getRepresentative().flatMap(Representative::getOrganisationContactDetails).flatMap(ContactDetails::getEmail).orElse(null))
                .build())
            .collect(JsonCollectors.toJsonArray());
    }

    private JsonArray mapDefendants(List<TheirDetails> defendants) {
        return defendants.stream()
            .map(defendant -> new NullAwareJsonObjectBuilder()
                .add("claimantProvidedDetail", mapDefendantDetails(defendant))
                .add("representativeOrganisationName", defendant.getRepresentative().map(Representative::getOrganisationName).orElse(null))
                .add("representativeOrganisationAddress", defendant.getRepresentative().map(Representative::getOrganisationAddress).map(this::mapAddress).orElse(null))
                .add("representativeOrganisationPhone", defendant.getRepresentative().flatMap(Representative::getOrganisationContactDetails).flatMap(ContactDetails::getPhone).orElse(null))
                .add("representativeOrganisationDxAddress", defendant.getRepresentative().flatMap(Representative::getOrganisationContactDetails).flatMap(ContactDetails::getDxAddress).orElse(null))
                .add("representativeOrganisationEmail", defendant.getRepresentative().flatMap(Representative::getOrganisationContactDetails).flatMap(ContactDetails::getEmail).orElse(null))
                .build())
            .collect(JsonCollectors.toJsonArray());
    }

    private JsonObject mapClaimantDetails(Party claimant, String submitterEmail) {
        return new NullAwareJsonObjectBuilder()
            .add("type", claimant.getClass().getSimpleName().toUpperCase())
            .add("primaryAddress", mapAddress(claimant.getAddress()))
            .add("emailAddress", submitterEmail)
            .add("telephoneNumber", new NullAwareJsonObjectBuilder().add("telephoneNumber", claimant.getPhone().orElse(null)).build())
            .add("dateOfBirth", getDateOfBirth(claimant))
            .build();
    }

    private String getDateOfBirth(Party claimant) {
        return claimant instanceof Individual
            ? Optional.ofNullable(((Individual) claimant).getDateOfBirth()).map(DateFormatter::format).orElse(null)
            : null;
    }

    private JsonObject mapDefendantDetails(TheirDetails defendant) {
        return new NullAwareJsonObjectBuilder()
            .add("type", defendant.getClass().getSimpleName().replace("Details", "").toUpperCase())
            .add("title", getDefendantTitle(defendant))
            .add("firstName", getDefendantFirstName(defendant))
            .add("lastName", getDefendantLastName(defendant))
            .add("name", defendant.getName())
            .add("primaryAddress", mapAddress(defendant.getAddress()))
            .add("emailAddress", defendant.getEmail().orElse(null))
            .build();
    }

    private String getDefendantTitle(TheirDetails defendant) {
        return defendant instanceof SoleTraderDetails
            ? extractOptionalFromSubclass(defendant, SoleTraderDetails.class, SoleTraderDetails::getTitle)
            : extractOptionalFromSubclass(defendant, IndividualDetails.class, IndividualDetails::getTitle);
    }

    private String getDefendantFirstName(TheirDetails defendant) {
        return defendant instanceof SoleTraderDetails
            ? extractFromSubclass(defendant, SoleTraderDetails.class, SoleTraderDetails::getFirstName)
            : extractFromSubclass(defendant, IndividualDetails.class, IndividualDetails::getFirstName);
    }

    private String getDefendantLastName(TheirDetails defendant) {
        return defendant instanceof SoleTraderDetails
            ? extractFromSubclass(defendant, SoleTraderDetails.class, SoleTraderDetails::getLastName)
            : extractFromSubclass(defendant, IndividualDetails.class, IndividualDetails::getLastName);
    }

    private JsonObject mapAddress(Address address) {
        return new NullAwareJsonObjectBuilder()
            .add("AddressLine1", address.getLine1())
            .add("AddressLine2", address.getLine2())
            .add("AddressLine3", address.getLine3())
            .add("PostTown", address.getCity())
            .add("PostCode", address.getPostcode())
            .build();
    }

}
