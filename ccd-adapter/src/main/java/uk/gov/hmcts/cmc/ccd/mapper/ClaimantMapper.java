package uk.gov.hmcts.cmc.ccd.mapper;

import org.apache.commons.lang3.StringUtils;
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

import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ISO_DATE;

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
                return new Company(ccdClaimant.getPartyName(), addressMapper.from(ccdClaimant.getPartyAddress()),
                    addressMapper.from(ccdClaimant.getPartyCorrespondenceAddress()), ccdClaimant.getPartyPhoneNumber(),
                    representativeMapper.from(ccdClaimant), ccdClaimant.getPartyContactPerson());
            case INDIVIDUAL:
                return new Individual(ccdClaimant.getPartyName(), addressMapper.from(ccdClaimant.getPartyAddress()),
                    addressMapper.from(ccdClaimant.getPartyCorrespondenceAddress()), ccdClaimant.getPartyPhoneNumber(),
                    representativeMapper.from(ccdClaimant),
                    parseDob(ccdClaimant.getPartyDateOfBirth()));
            case SOLE_TRADER:
                return new SoleTrader(ccdClaimant.getPartyName(), addressMapper.from(ccdClaimant.getPartyAddress()),
                    addressMapper.from(ccdClaimant.getPartyCorrespondenceAddress()), ccdClaimant.getPartyPhoneNumber(),
                    representativeMapper.from(ccdClaimant),
                    ccdClaimant.getPartyTitle(), ccdClaimant.getPartyBusinessName());
            case ORGANISATION:
                return new Organisation(ccdClaimant.getPartyName(), addressMapper.from(ccdClaimant.getPartyAddress()),
                    addressMapper.from(ccdClaimant.getPartyCorrespondenceAddress()), ccdClaimant.getPartyPhoneNumber(),
                    representativeMapper.from(ccdClaimant),
                    ccdClaimant.getPartyContactPerson(), ccdClaimant.getPartyCompaniesHouseNumber());
            default:
                throw new MappingException();
        }
    }

    private LocalDate parseDob(String dateOfBirth) {
        if (StringUtils.isBlank(dateOfBirth)) {
            return null;
        }

        return LocalDate.parse(dateOfBirth, ISO_DATE);
    }
}
