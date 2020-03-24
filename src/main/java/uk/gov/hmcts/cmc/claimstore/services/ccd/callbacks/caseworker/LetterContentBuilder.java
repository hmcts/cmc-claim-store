package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactChangeContent;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDTelephone;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;

@Service
public class LetterContentBuilder {

    public CCDContactChangeContent letterContent(CCDParty partyBefore, CCDParty partyNow) {

        CCDContactChangeContent.CCDContactChangeContentBuilder contactChangeContent = CCDContactChangeContent.builder()
            .isPrimaryAddressModified(NO)
            .isEmailModified(NO)
            .isCorrespondenceAddressModified(NO)
            .isTelephoneModified(NO)
            .correspondenceAddressRemoved(NO)
            .primaryEmailRemoved(NO)
            .telephoneRemoved(NO);

        CCDAddress oldAddress = partyBefore.getPrimaryAddress();
        CCDAddress newAddress = partyNow.getPrimaryAddress();

        if (contentDiffer(oldAddress, newAddress)) {
            contactChangeContent.primaryAddress(newAddress);
            contactChangeContent.isPrimaryAddressModified(YES);
        }

        String oldEmail = partyBefore.getEmailAddress();
        String newEmail = partyNow.getEmailAddress();

        if (contentDiffer(oldEmail, newEmail)) {
            if (StringUtils.isBlank(newEmail)) {
                contactChangeContent.primaryEmailRemoved(YES);
            } else {
                contactChangeContent.isEmailModified(YES);
                contactChangeContent.primaryEmail(newEmail);
            }
        }

        CCDAddress oldCorrespondenceAddress = partyBefore.getCorrespondenceAddress();
        CCDAddress newCorrespondenceAddress = partyNow.getCorrespondenceAddress();

        if (contentDiffer(oldCorrespondenceAddress, newCorrespondenceAddress)) {
            if (!Optional.ofNullable(newCorrespondenceAddress).isPresent()) {
                contactChangeContent.correspondenceAddressRemoved(YES);
            } else {
                contactChangeContent.correspondenceAddress(newCorrespondenceAddress);
                contactChangeContent.isCorrespondenceAddressModified(YES);
            }
        }

        CCDTelephone oldPhone = partyBefore.getTelephoneNumber();
        CCDTelephone newPhone = partyNow.getTelephoneNumber();

        if (contentDiffer(oldPhone, newPhone)) {
            if (!Optional.ofNullable(newPhone).isPresent()) {
                contactChangeContent.telephoneRemoved(YES);
            } else {
                contactChangeContent.isTelephoneModified(YES);
                contactChangeContent.telephone(newPhone.getTelephoneNumber());
            }
        }

        return contactChangeContent.build();
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
