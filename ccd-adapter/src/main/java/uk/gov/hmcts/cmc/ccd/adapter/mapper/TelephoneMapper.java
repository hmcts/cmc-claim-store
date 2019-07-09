package uk.gov.hmcts.cmc.ccd.adapter.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDTelephone;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class TelephoneMapper implements Mapper<CCDTelephone, String> {

    @Override
    public CCDTelephone to(String telephoneNumber) {
        if (isBlank(telephoneNumber)) {
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
