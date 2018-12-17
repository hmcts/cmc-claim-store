package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;

@Component
public class DefendantMapper implements Mapper<CCDDefendant, TheirDetails> {

    private final IndividualDetailsMapper individualDetailsMapper;
    private final CompanyDetailsMapper companyDetailsMapper;
    private final OrganisationDetailsMapper organisationDetailsMapper;
    private final SoleTraderDetailsMapper soleTraderDetailsMapper;

    @Autowired
    public DefendantMapper(
        IndividualDetailsMapper individualDetailsMapper,
        CompanyDetailsMapper companyDetailsMapper,
        OrganisationDetailsMapper organisationDetailsMapper,
        SoleTraderDetailsMapper soleTraderDetailsMapper
    ) {

        this.individualDetailsMapper = individualDetailsMapper;
        this.companyDetailsMapper = companyDetailsMapper;
        this.organisationDetailsMapper = organisationDetailsMapper;
        this.soleTraderDetailsMapper = soleTraderDetailsMapper;
    }

    @Override
    public CCDDefendant to(TheirDetails party) {
        CCDDefendant.CCDDefendantBuilder builder = CCDDefendant.builder();

        if (party instanceof IndividualDetails) {
            builder.partyType(CCDPartyType.INDIVIDUAL);
            IndividualDetails individual = (IndividualDetails) party;
            individualDetailsMapper.to(individual, builder);
        } else if (party instanceof CompanyDetails) {
            builder.partyType(CCDPartyType.COMPANY);
            CompanyDetails company = (CompanyDetails) party;
            companyDetailsMapper.to(company, builder);
        } else if (party instanceof OrganisationDetails) {
            builder.partyType(CCDPartyType.ORGANISATION);
            OrganisationDetails organisation = (OrganisationDetails) party;
            organisationDetailsMapper.to(organisation, builder);
        } else if (party instanceof SoleTraderDetails) {
            builder.partyType(CCDPartyType.SOLE_TRADER);
            SoleTraderDetails soleTrader = (SoleTraderDetails) party;
            soleTraderDetailsMapper.to(soleTrader, builder);
        }
        return builder.build();
    }

    @Override
    public TheirDetails from(CCDDefendant ccdDefendant) {
        switch (ccdDefendant.getPartyType()) {
            case COMPANY:
                return companyDetailsMapper.from(ccdDefendant);
            case INDIVIDUAL:
                return individualDetailsMapper.from(ccdDefendant);
            case SOLE_TRADER:
                return soleTraderDetailsMapper.from(ccdDefendant);
            case ORGANISATION:
                return organisationDetailsMapper.from(ccdDefendant);
            default:
                throw new MappingException();
        }
    }
}
