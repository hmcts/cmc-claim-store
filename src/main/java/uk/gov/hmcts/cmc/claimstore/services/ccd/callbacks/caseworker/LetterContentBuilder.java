package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class LetterContentBuilder {
    public static final String MAIN_ADDRESS_CHANGE = "Their address is now:: %s ";
    public static final String CONTACT_ADDRESS_CHANGE = "The address they want to use for post about the claim is now:: %s";
    public static final String TELEPHONE_CHANGE = "Their phone number is now:: %s";
    public static final String EMAIL_CHANGE = "Their email address is now:: %s";
    public static final String EMAIL_REMOVED = "They’ve removed their email address.";
    public static final String CONTACT_ADDRESS_REMOVED = "They’ve removed the address they want to use for post about the claim.";
    public static final String PHONE_REMOVED = "They’ve removed their phone number.";

    public String letterContent(Claim claimBefore, Claim claimNow){

        StringBuilder letterContent = new StringBuilder("This will be sent to them as a letter or email.");

        Address oldAddress = claimBefore.getClaimData().getClaimant().getAddress();
        Address newAddress = claimNow.getClaimData().getClaimant().getAddress();
        if (!oldAddress.equals(newAddress)) {
            letterContent.append(String.format(MAIN_ADDRESS_CHANGE, toString(newAddress)));
        }

        String oldEmail = claimBefore.getSubmitterEmail();
        String newEmail = claimNow.getSubmitterEmail();

        if (!oldEmail.equals(newEmail)) {
            letterContent.append(String.format(EMAIL_CHANGE, newEmail));
            if (newEmail.isEmpty()) {
                letterContent.append(EMAIL_REMOVED);
            }
        }

        Address oldCorrespondenceAddress = claimBefore.getClaimData().getClaimant()
            .getCorrespondenceAddress().orElse(null);
        Address newCorrespondenceAddress = claimNow.getClaimData().getClaimant()
            .getCorrespondenceAddress().orElse(null);

        if (contentDiffer(oldCorrespondenceAddress, newCorrespondenceAddress)) {
            letterContent.append(String.format(CONTACT_ADDRESS_CHANGE, toString(newCorrespondenceAddress)));
            if (!Optional.ofNullable(newCorrespondenceAddress).isPresent()) {
                letterContent.append(CONTACT_ADDRESS_REMOVED);
            }
        }

        String oldPhone = claimBefore.getClaimData().getClaimant().getPhone().orElse(null);
        String newPhone = claimNow.getClaimData().getClaimant().getPhone().orElse(null);

        if (contentDiffer(oldPhone, newPhone)) {
            letterContent.append(String.format(TELEPHONE_CHANGE, newPhone.toString()));
            if (!Optional.ofNullable(newPhone).isPresent()) {
                letterContent.append(PHONE_REMOVED);
            }
        }

        return letterContent.toString();
    }

    private String toString(Address newAddress) {
        return Stream.of(newAddress.getLine1(), newAddress.getLine2(), newAddress.getLine3(), newAddress.getCity(), newAddress.getPostcode())
            .map(s -> s.replaceAll("\\r\\n|\\r|\\n", ""))
            .collect(Collectors.joining("\n"));
    }

    private boolean contentDiffer(Object old, Object latest){

        if(old == null && latest == null){
            return false;
        }

        if(old == null){
            return true;
        }

        return !old.equals(latest);
    }
}
