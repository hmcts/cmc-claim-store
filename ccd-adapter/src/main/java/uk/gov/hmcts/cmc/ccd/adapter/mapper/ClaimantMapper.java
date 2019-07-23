package uk.gov.hmcts.cmc.ccd.adapter.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.adapter.exception.MappingException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;

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

    public CCDCollectionElement<CCDApplicant> to(Party party, Claim claim, boolean leadApplicant) {
        CCDApplicant.CCDApplicantBuilder builder = CCDApplicant.builder();
        builder.leadApplicantIndicator(leadApplicant ? YES : NO);
        CCDParty.CCDPartyBuilder partyDetail = CCDParty.builder();
        partyDetail.idamId(claim.getSubmitterId());
        partyDetail.emailAddress(claim.getSubmitterEmail());

        if (party instanceof Individual) {
            Individual individual = (Individual) party;
            individualMapper.to(individual, builder, partyDetail);
        } else if (party instanceof Company) {
            Company company = (Company) party;
            companyMapper.to(company, builder, partyDetail);
        } else if (party instanceof Organisation) {
            Organisation organisation = (Organisation) party;
            organisationMapper.to(organisation, builder, partyDetail);
        } else if (party instanceof SoleTrader) {
            SoleTrader soleTrader = (SoleTrader) party;
            soleTraderMapper.to(soleTrader, builder, partyDetail);
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
