package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

@Component
public class PartyMapper implements Mapper<CCDParty, Party> {

    private final IndividualMapper individualMapper;
    private final CompanyMapper companyMapper;
    private final OrganisationMapper organisationMapper;
    private final SoleTraderMapper soleTraderMapper;

    @Autowired
    public PartyMapper(IndividualMapper individualMapper, CompanyMapper companyMapper,
                       OrganisationMapper organisationMapper, SoleTraderMapper soleTraderMapper) {
        this.individualMapper = individualMapper;
        this.companyMapper = companyMapper;
        this.organisationMapper = organisationMapper;
        this.soleTraderMapper = soleTraderMapper;
    }

    @Override
    public CCDParty to(Party party) {
        final CCDParty.CCDPartyBuilder builder = CCDParty.builder();

        if (party instanceof Individual) {
            builder.type(CCDPartyType.INDIVIDUAL);
            Individual individual = (Individual) party;
            builder.individual(individualMapper.to(individual));
        } else if (party instanceof Company) {
            builder.type(CCDPartyType.COMPANY);
            Company company = (Company) party;
            builder.company(companyMapper.to(company));
        } else if (party instanceof Organisation) {
            builder.type(CCDPartyType.ORGANISATION);
            Organisation organisation = (Organisation) party;
            builder.organisation(organisationMapper.to(organisation));
        } else if (party instanceof SoleTrader) {
            builder.type(CCDPartyType.SOLE_TRADER);
            SoleTrader soleTrader = (SoleTrader) party;
            builder.soleTrader(soleTraderMapper.to(soleTrader));
        }

        return builder.build();
    }

    @Override
    public Party from(CCDParty ccdParty) {
        return null;
    }
}
