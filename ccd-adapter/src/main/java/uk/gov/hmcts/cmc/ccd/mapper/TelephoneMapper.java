package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDTelephone;

@Component
public class TelephoneMapper implements Mapper<CCDTelephone, String> {

    @Override
    public CCDTelephone to(String telephoneNumber) {
        if (telephoneNumber == null) {
            return null;
        }
        return CCDTelephone.builder()
            .telephoneNumber(telephoneNumber)
            .build();
    }

    @Override
    public String from(CCDTelephone ccdTelephone) {
        if (ccdTelephone == null) {
            return null;
        }
        return ccdTelephone.getTelephoneNumber();
    }
}
