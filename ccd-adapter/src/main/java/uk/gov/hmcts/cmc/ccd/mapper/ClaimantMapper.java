package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
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

    public CCDCollectionElement<CCDApplicant> to(Party party, Claim claim) {
        CCDApplicant.CCDApplicantBuilder builder = CCDApplicant.builder();
        CCDParty.CCDPartyBuilder partyDetail = CCDParty.builder();
        partyDetail.emailAddress(claim.getSubmitterEmail());

        if (party instanceof Individual) {
            Individual individual = (Individual) party;
            individualMapper.to(individual, builder);
        } else if (party instanceof Company) {
            Company company = (Company) party;
            companyMapper.to(company, builder);
        } else if (party instanceof Organisation) {
            Organisation organisation = (Organisation) party;
            organisationMapper.to(organisation, builder);
        } else if (party instanceof SoleTrader) {
            SoleTrader soleTrader = (SoleTrader) party;
            soleTraderMapper.to(soleTrader, builder);
        }
        return CCDCollectionElement.<CCDApplicant>builder()
            .value(builder.build())
            .id(party.getId())
            .build();
    }

    public Party from(CCDCollectionElement<CCDApplicant> applicant) {
        switch (applicant.getValue().getPartyDetail().getType()) {
            case COMPANY:
                return companyMapper.from(applicant);
            case INDIVIDUAL:
                return individualMapper.from(applicant);
            case SOLE_TRADER:
                return soleTraderMapper.from(applicant);
            case ORGANISATION:
                return organisationMapper.from(applicant);
            default:
                throw new MappingException("Invalid applicant type, "
                    + applicant.getValue().getPartyDetail().getType());
        }
    }
}
