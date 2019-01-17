package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

@Component
public class ClaimantMapper {

    private final IndividualMapper individualMapper;
    private final CompanyMapper companyMapper;
    private final OrganisationMapper organisationMapper;
    private final SoleTraderMapper soleTraderMapper;

    public ClaimantMapper(IndividualMapper individualMapper,
                          CompanyMapper companyMapper,
                          OrganisationMapper organisationMapper,
                          SoleTraderMapper soleTraderMapper) {

        this.individualMapper = individualMapper;
        this.companyMapper = companyMapper;
        this.organisationMapper = organisationMapper;
        this.soleTraderMapper = soleTraderMapper;
    }

    public CCDClaimant to(Party party, Claim claim) {
        CCDClaimant.CCDClaimantBuilder builder = CCDClaimant.builder();
        builder.partyEmail(claim.getSubmitterEmail());

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
                throw new MappingException("Invalid claimant type, " + ccdClaimant.getPartyType());
        }
    }
}
