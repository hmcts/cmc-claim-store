package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

@Component
public class SoleTraderMapper {

    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;
    private final TelephoneMapper telephoneMapper;

    @Autowired
    public SoleTraderMapper(AddressMapper addressMapper, RepresentativeMapper representativeMapper,
                            TelephoneMapper telephoneMapper) {
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
        this.telephoneMapper = telephoneMapper;
    }

    public void to(SoleTrader soleTrader, CCDApplicant.CCDApplicantBuilder builder) {
        CCDParty.CCDPartyBuilder partyDetails = CCDParty.builder();
        soleTrader.getTitle().ifPresent(partyDetails::title);
        soleTrader.getMobilePhone()
            .ifPresent(telephoneNo -> partyDetails.telephoneNumber(telephoneMapper.to(telephoneNo)));
        soleTrader.getBusinessName().ifPresent(partyDetails::businessName);
        soleTrader.getCorrespondenceAddress()
            .ifPresent(address -> partyDetails.correspondenceAddress(addressMapper.to(address)));
        soleTrader.getRepresentative()
            .ifPresent(representative -> representativeMapper.to(representative, builder));
        partyDetails.primaryAddress(addressMapper.to(soleTrader.getAddress()));
        builder
            .partyName(soleTrader.getName())
            .partyDetail(partyDetails.build());
    }

    public SoleTrader from(CCDCollectionElement<CCDApplicant> soletrader) {
        CCDApplicant applicant = soletrader.getValue();
        CCDParty partyDetail = applicant.getPartyDetail();
        return SoleTrader.builder()
            .id(soletrader.getId())
            .name(applicant.getPartyName())
            .address(addressMapper.from(partyDetail.getPrimaryAddress()))
            .correspondenceAddress(addressMapper.from(partyDetail.getCorrespondenceAddress()))
            .mobilePhone(telephoneMapper.from(partyDetail.getTelephoneNumber()))
            .representative(representativeMapper.from(applicant))
            .title(partyDetail.getTitle())
            .businessName(partyDetail.getBusinessName())
            .build();
    }
}
