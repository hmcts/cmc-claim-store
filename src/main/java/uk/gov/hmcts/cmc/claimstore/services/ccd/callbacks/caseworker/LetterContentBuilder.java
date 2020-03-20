package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class LetterContentBuilder {

    public static final String MAIN_ADDRESS_CHANGE = "\nTheir address is now:: %s";
    public static final String CONTACT_ADDRESS_CHANGE = "\nThe address they want to use for post about the claim is now:: %s";
    public static final String TELEPHONE_CHANGE = "\nTheir phone number is now:: %s";
    public static final String EMAIL_CHANGE = "\nTheir email address is now:: %s";
    public static final String EMAIL_REMOVED = "\nThey’ve removed their email address.";
    public static final String CONTACT_ADDRESS_REMOVED = "\nThey’ve removed the address they want to use for post about the claim.";
    public static final String PHONE_REMOVED = "\nThey’ve removed their phone number.";
    public static final String MAIN_MESSAGE = "\nWe’re contacting you because %s has changed their contact details.";

    public String letterContent(Claim claimBefore, Claim claimNow) {


        Party claimant = claimBefore.getClaimData().getClaimant();
        Address oldAddress = claimant.getAddress();
        Address newAddress = claimNow.getClaimData().getClaimant().getAddress();

        StringBuilder letterContent = new StringBuilder(String.format(MAIN_MESSAGE, claimant.getName()));
        if (!oldAddress.equals(newAddress)) {
            letterContent.append(String.format(MAIN_ADDRESS_CHANGE, toString(newAddress)));
            letterContent.append(System.lineSeparator());
        }

        String oldEmail = claimBefore.getSubmitterEmail();
        String newEmail = claimNow.getSubmitterEmail();

        if (contentDiffer(oldEmail, newEmail)) {
            if (newEmail.isEmpty()) {
                letterContent.append(EMAIL_REMOVED);
            } else {
                letterContent.append(String.format(EMAIL_CHANGE, newEmail));
            }
            letterContent.append(System.lineSeparator());
        }

        Address oldCorrespondenceAddress = claimant
            .getCorrespondenceAddress().orElse(null);
        Address newCorrespondenceAddress = claimNow.getClaimData().getClaimant()
            .getCorrespondenceAddress().orElse(null);

        if (contentDiffer(oldCorrespondenceAddress, newCorrespondenceAddress)) {
            if (!Optional.ofNullable(newCorrespondenceAddress).isPresent()) {
                letterContent.append(CONTACT_ADDRESS_REMOVED);
            } else {
                letterContent.append(String.format(CONTACT_ADDRESS_CHANGE, toString(newCorrespondenceAddress)));
            }
            letterContent.append(System.lineSeparator());
        }

        String oldPhone = claimant.getPhone().orElse(null);
        String newPhone = claimNow.getClaimData().getClaimant().getPhone().orElse(null);

        if (contentDiffer(oldPhone, newPhone)) {
            if (!Optional.ofNullable(newPhone).isPresent()) {
                letterContent.append(PHONE_REMOVED);
            } else {
                letterContent.append(String.format(TELEPHONE_CHANGE, newPhone));
            }
            letterContent.append(System.lineSeparator());
        }

        return letterContent.toString();
    }

    private String toString(Address newAddress) {
        return Stream.of(newAddress.getLine1(), newAddress.getLine2(), newAddress.getLine3(), newAddress.getCity(), newAddress.getPostcode())
            .map(s -> s.replaceAll("\\r\\n|\\r|\\n", ""))
            .collect(Collectors.joining("\n"));
    }

    private boolean contentDiffer(Object old, Object latest) {

        if (old == null && latest == null) {
            return false;
        }

        if (old == null) {
            return true;
        }

        return !old.equals(latest);
    }
}
