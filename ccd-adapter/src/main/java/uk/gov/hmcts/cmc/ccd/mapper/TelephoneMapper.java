package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDTelephone;

@Component
public class TelephoneMapper implements Mapper<CCDTelephone, String> {

    private TelephoneMapper(){
        // Do nothing
    }

    @Override
    public CCDTelephone to(String telephoneNumber) {
        return CCDTelephone.builder()
            .telephoneNumber(telephoneNumber)
            .build();
    }

    @Override
    public String from(CCDTelephone ccdTelephone) {
        return ccdTelephone.getTelephoneNumber();
    }
}
