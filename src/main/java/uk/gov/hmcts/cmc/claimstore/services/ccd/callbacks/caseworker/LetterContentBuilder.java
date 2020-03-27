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

    public CCDContactChangeContent letterContent(CCDParty previousPartyDetails, CCDParty latestPartyDetails) {

        CCDContactChangeContent.CCDContactChangeContentBuilder contactChangeContent = CCDContactChangeContent.builder()
            .isPrimaryAddressModified(NO)
            .isEmailModified(NO)
            .isCorrespondenceAddressModified(NO)
            .isTelephoneModified(NO)
            .correspondenceAddressRemoved(NO)
            .primaryEmailRemoved(NO)
            .telephoneRemoved(NO);

        if (previousPartyDetails == null) {
            updateFlagsBasedOnLatestPartyDetails(latestPartyDetails, contactChangeContent);
        } else {
            UpdateFlagsByComparingPartDetails(previousPartyDetails, latestPartyDetails, contactChangeContent);
        }
        return contactChangeContent.build();
    }

    private void UpdateFlagsByComparingPartDetails(
        CCDParty partyBefore,
        CCDParty partyNow,
        CCDContactChangeContent.CCDContactChangeContentBuilder contactChangeContent
    ) {
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
                contactChangeContent.telephone(newPhone.getTelephoneNumber());
                contactChangeContent.isTelephoneModified(YES);
            }
        }
    }

    private void updateFlagsBasedOnLatestPartyDetails(
        CCDParty partyNow,
        CCDContactChangeContent.CCDContactChangeContentBuilder contactChangeContent
    ) {
        if (partyNow.getCorrespondenceAddress() != null) {
            contactChangeContent.correspondenceAddress(partyNow.getCorrespondenceAddress())
                .isCorrespondenceAddressModified(YES);
        }

        if (partyNow.getPrimaryAddress() != null) {
            contactChangeContent.primaryAddress(partyNow.getPrimaryAddress())
                .isPrimaryAddressModified(YES);
        }

        if (!StringUtils.isBlank(partyNow.getEmailAddress())) {
            contactChangeContent.isEmailModified(YES)
                .primaryEmail(partyNow.getEmailAddress());
        }

        if (partyNow.getTelephoneNumber() != null) {
            contactChangeContent.telephone(partyNow.getTelephoneNumber().getTelephoneNumber())
                .isTelephoneModified(YES);
        }
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
