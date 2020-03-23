package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDTelephone;
import uk.gov.hmcts.cmc.ccd.domain.ContactChangeContent;
import uk.gov.hmcts.cmc.domain.models.Address;

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

    public ContactChangeContent letterContent(CCDCase caseBefore, CCDCase caseNow) {


        CCDApplicant claimant = caseBefore.getApplicants().get(0).getValue();
        CCDApplicant claimantNow = caseNow.getApplicants().get(0).getValue();
        CCDAddress oldAddress = claimant.getPartyDetail().getPrimaryAddress();
        CCDAddress newAddress = claimantNow.getPartyDetail().getPrimaryAddress();

        ContactChangeContent.ContactChangeContentBuilder contactChangeContent = ContactChangeContent.builder();
        if (!oldAddress.equals(newAddress)) {
            contactChangeContent.claimantAddress(newAddress);
            contactChangeContent.hasMainAddressChanged(true);
        }

        String oldEmail = claimant.getPartyDetail().getEmailAddress();
        String newEmail = claimantNow.getPartyDetail().getEmailAddress();

        if (contentDiffer(oldEmail, newEmail)) {
            if (newEmail.isEmpty()) {
                contactChangeContent.claimantEmailRemoved(true);
            } else {
                contactChangeContent.claimantEmail(newEmail);
            }
        }

        CCDAddress oldCorrespondenceAddress = claimant.getPartyDetail().getCorrespondenceAddress();
        CCDAddress newCorrespondenceAddress = claimantNow.getPartyDetail().getCorrespondenceAddress();

        if (contentDiffer(oldCorrespondenceAddress, newCorrespondenceAddress)) {
            if (!Optional.ofNullable(newCorrespondenceAddress).isPresent()) {
                contactChangeContent.claimantContactAddressRemoved(true);
            } else {
                contactChangeContent.claimantContactAddress(newCorrespondenceAddress);
                contactChangeContent.hasContactAddressChanged(true);
            }
        }

        CCDTelephone oldPhone = claimant.getPartyDetail().getTelephoneNumber();
        CCDTelephone newPhone = claimantNow.getPartyDetail().getTelephoneNumber();

        if (contentDiffer(oldPhone, newPhone)) {
            if (!Optional.ofNullable(newPhone).isPresent()) {
                contactChangeContent.claimantPhoneRemoved(true);
            } else {
                contactChangeContent.claimantPhone(newPhone.getTelephoneNumber());
            }
        }

        return contactChangeContent.build();
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
