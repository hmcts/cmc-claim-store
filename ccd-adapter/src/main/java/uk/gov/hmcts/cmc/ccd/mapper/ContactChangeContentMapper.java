package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactChangeContent;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.ContactChangeContent;

@Component
public class ContactChangeContentMapper {
    public CCDContactChangeContent from(ContactChangeContent contactChangeContent) {
        return CCDContactChangeContent.builder()
            .caseworkerName(contactChangeContent.getCaseworkerName())
            .claimantEmail(contactChangeContent.getClaimantEmail())
            .claimantPhone(contactChangeContent.getClaimantPhone())
            .hasEmailChanged(CCDYesNoOption.valueOf(contactChangeContent.isHasEmailChanged()))
            .hasPhoneChanged(CCDYesNoOption.valueOf(contactChangeContent.isHasPhoneChanged()))
            .hasContactAddressChanged(CCDYesNoOption.valueOf(contactChangeContent.isHasContactAddressChanged()))
            .hasMainAddressChanged(CCDYesNoOption.valueOf(contactChangeContent.isHasMainAddressChanged()))
            .claimantAddress(contactChangeContent.getClaimantAddress())
            .claimantContactAddress(contactChangeContent.getClaimantContactAddress())
            .claimantPhoneRemoved(CCDYesNoOption.valueOf(contactChangeContent.isClaimantPhoneRemoved()))
            .claimantEmailRemoved(CCDYesNoOption.valueOf(contactChangeContent.isClaimantEmailRemoved()))
            .claimantContactAddressRemoved(CCDYesNoOption.valueOf(contactChangeContent.isClaimantContactAddressRemoved()))
            .build();
    }
}
