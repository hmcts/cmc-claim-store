package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDCompany;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDIndividual;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDOrganisation;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDSoleTrader;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ISO_DATE;

//@Component
public class PartyMapper implements Mapper<CCDParty, Party> {

    private final IndividualMapper individualMapper;
    private final CompanyMapper companyMapper;
    private final OrganisationMapper organisationMapper;
    private final SoleTraderMapper soleTraderMapper;
    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    @Autowired
    public PartyMapper(IndividualMapper individualMapper,
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
    public CCDParty to(Party party) {
        CCDParty.CCDPartyBuilder builder = CCDParty.builder();

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
        switch (ccdParty.getType()) {
            case COMPANY:
                CCDCompany ccdCompany = ccdParty.getCompany();
                return new Company(ccdCompany.getName(), addressMapper.from(ccdCompany.getAddress()),
                    addressMapper.from(ccdCompany.getCorrespondenceAddress()), ccdCompany.getPhoneNumber(),
                    representativeMapper.from(ccdCompany.getRepresentative()), ccdCompany.getContactPerson());
            case INDIVIDUAL:
                CCDIndividual ccdIndividual = ccdParty.getIndividual();
                return new Individual(ccdIndividual.getName(), addressMapper.from(ccdIndividual.getAddress()),
                    addressMapper.from(ccdIndividual.getCorrespondenceAddress()), ccdIndividual.getPhoneNumber(),
                    representativeMapper.from(ccdIndividual.getRepresentative()),
                    parseDob(ccdIndividual.getDateOfBirth()));
            case SOLE_TRADER:
                CCDSoleTrader ccdSoleTrader = ccdParty.getSoleTrader();
                return new SoleTrader(ccdSoleTrader.getName(), addressMapper.from(ccdSoleTrader.getAddress()),
                    addressMapper.from(ccdSoleTrader.getCorrespondenceAddress()),
                    ccdSoleTrader.getPhoneNumber(), representativeMapper.from(ccdSoleTrader.getRepresentative()),
                    ccdSoleTrader.getTitle(), ccdSoleTrader.getBusinessName());
            case ORGANISATION:
                CCDOrganisation ccdOrganisation = ccdParty.getOrganisation();
                return new Organisation(ccdOrganisation.getName(), addressMapper.from(ccdOrganisation.getAddress()),
                    addressMapper.from(ccdOrganisation.getCorrespondenceAddress()),
                    ccdOrganisation.getPhoneNumber(), representativeMapper.from(ccdOrganisation.getRepresentative()),
                    ccdOrganisation.getContactPerson(), ccdOrganisation.getCompaniesHouseNumber());
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
