package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

@Component
public class ClaimantMapper implements Mapper<CCDClaimant, Party> {

    private final IndividualMapper individualMapper;
    private final CompanyMapper companyMapper;
    private final OrganisationMapper organisationMapper;
    private final SoleTraderMapper soleTraderMapper;
    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public ClaimantMapper(IndividualMapper individualMapper,
                          CompanyMapper companyMapper,
                          OrganisationMapper organisationMapper,
                          SoleTraderMapper soleTraderMapper,
                          AddressMapper addressMapper,
                          RepresentativeMapper representativeMapper) {

        this.individualMapper = individualMapper;
        this.companyMapper = companyMapper;
        this.organisationMapper = organisationMapper;
        this.soleTraderMapper = soleTraderMapper;
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
    public CCDClaimant to(Party party) {
        CCDClaimant.CCDClaimantBuilder builder = CCDClaimant.builder();

        if (party instanceof Individual) {
            builder.partyType(CCDPartyType.INDIVIDUAL);
            Individual individual = (Individual) party;
            individualMapper.to(individual, builder);
        } else if (party instanceof Company) {
            builder.partyType(CCDPartyType.COMPANY);
            Company company = (Company) party;
            companyMapper.to(company, builder);
        } else if (party instanceof Organisation) {
            builder.partyType(CCDPartyType.ORGANISATION);
            Organisation organisation = (Organisation) party;
            organisationMapper.to(organisation, builder);
        } else if (party instanceof SoleTrader) {
            builder.partyType(CCDPartyType.SOLE_TRADER);
            SoleTrader soleTrader = (SoleTrader) party;
            soleTraderMapper.to(soleTrader, builder);
        }
        return builder.build();
    }

    @Override
    public Party from(CCDClaimant ccdClaimant) {
        switch (ccdClaimant.getPartyType()) {
            case COMPANY:
                return companyMapper.from(ccdClaimant);
            case INDIVIDUAL:
                return individualMapper.from(ccdClaimant);
            case SOLE_TRADER:
                return soleTraderMapper.from(ccdClaimant);
            case ORGANISATION:
                return organisationMapper.from(ccdClaimant);
            default:
                throw new MappingException();
        }
    }
}
