package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactChangeContent;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDTelephone;
import uk.gov.hmcts.cmc.domain.models.Address;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;

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

    public CCDContactChangeContent letterContent(CCDParty partyBefore, CCDParty partyNow) {

        CCDAddress oldAddress = partyBefore.getPrimaryAddress();
        CCDAddress newAddress = partyBefore.getPrimaryAddress();

        CCDContactChangeContent.CCDContactChangeContentBuilder contactChangeContent = CCDContactChangeContent.builder()
            .mainAddressChanged(NO)
            .claimantEmailChanged(NO)
            .contactAddressChanged(NO)
            .claimantPhoneChanged(NO);

        if (!oldAddress.equals(newAddress)) {
            contactChangeContent.claimantAddress(newAddress);
            contactChangeContent.mainAddressChanged(YES);
        }

        String oldEmail = partyBefore.getEmailAddress();
        String newEmail = partyNow.getEmailAddress();

        if (contentDiffer(oldEmail, newEmail)) {
            if (StringUtils.isBlank(newEmail)) {
                contactChangeContent.claimantEmailRemoved(YES);
            } else {
                contactChangeContent.claimantEmailRemoved(NO);
                contactChangeContent.claimantEmailChanged(YES);
                contactChangeContent.claimantEmail(newEmail);
            }
        }

        CCDAddress oldCorrespondenceAddress = partyBefore.getCorrespondenceAddress();
        CCDAddress newCorrespondenceAddress = partyNow.getCorrespondenceAddress();

        if (contentDiffer(oldCorrespondenceAddress, newCorrespondenceAddress)) {
            if (!Optional.ofNullable(newCorrespondenceAddress).isPresent()) {
                contactChangeContent.claimantContactAddressRemoved(YES);
            } else {
                contactChangeContent.claimantContactAddressRemoved(NO);
                contactChangeContent.claimantContactAddress(newCorrespondenceAddress);
                contactChangeContent.contactAddressChanged(YES);
            }
        }

        CCDTelephone oldPhone = partyBefore.getTelephoneNumber();
        CCDTelephone newPhone = partyNow.getTelephoneNumber();

        if (contentDiffer(oldPhone, newPhone)) {
            if (!Optional.ofNullable(newPhone).isPresent()) {
                contactChangeContent.claimantPhoneRemoved(YES);
            } else {
                contactChangeContent.claimantPhoneRemoved(NO);
                contactChangeContent.claimantPhoneChanged(YES);
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
