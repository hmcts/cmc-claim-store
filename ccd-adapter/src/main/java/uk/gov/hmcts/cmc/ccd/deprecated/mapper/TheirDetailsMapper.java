package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDCompany;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDIndividual;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDOrganisation;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDSoleTrader;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.defendant.CompanyDetailsMapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.defendant.IndividualDetailsMapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.defendant.OrganisationDetailsMapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.defendant.SoleTraderDetailsMapper;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;

import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPartyType.COMPANY;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPartyType.INDIVIDUAL;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPartyType.ORGANISATION;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPartyType.SOLE_TRADER;

//@Component
public class TheirDetailsMapper implements Mapper<CCDParty, TheirDetails> {

    private final IndividualDetailsMapper individualDetailsMapper;
    private final CompanyDetailsMapper companyDetailsMapper;
    private final OrganisationDetailsMapper organisationDetailsMapper;
    private final SoleTraderDetailsMapper soleTraderDetailsMapper;
    private final AddressMapper addressMapper;
    private final RepresentativeMapper representativeMapper;

    public TheirDetailsMapper(IndividualDetailsMapper individualDetailsMapper,
                              CompanyDetailsMapper companyDetailsMapper,
                              OrganisationDetailsMapper organisationDetailsMapper,
                              SoleTraderDetailsMapper soleTraderDetailsMapper,
                              AddressMapper addressMapper,
                              RepresentativeMapper representativeMapper) {

        this.individualDetailsMapper = individualDetailsMapper;
        this.companyDetailsMapper = companyDetailsMapper;
        this.organisationDetailsMapper = organisationDetailsMapper;
        this.soleTraderDetailsMapper = soleTraderDetailsMapper;
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
    }

    @Override
    public CCDParty to(TheirDetails theirDetails) {
        CCDParty.CCDPartyBuilder builder = CCDParty.builder();

        if (theirDetails instanceof IndividualDetails) {
            builder.type(INDIVIDUAL);
            IndividualDetails individual = (IndividualDetails) theirDetails;
            builder.individual(individualDetailsMapper.to(individual));
        } else if (theirDetails instanceof CompanyDetails) {
            builder.type(COMPANY);
            CompanyDetails company = (CompanyDetails) theirDetails;
            builder.company(companyDetailsMapper.to(company));
        } else if (theirDetails instanceof OrganisationDetails) {
            builder.type(ORGANISATION);
            OrganisationDetails organisation = (OrganisationDetails) theirDetails;
            builder.organisation(organisationDetailsMapper.to(organisation));
        } else if (theirDetails instanceof SoleTraderDetails) {
            builder.type(SOLE_TRADER);
            SoleTraderDetails soleTrader = (SoleTraderDetails) theirDetails;
            builder.soleTrader(soleTraderDetailsMapper.to(soleTrader));
        }

        return builder.build();
    }

    @Override
    public TheirDetails from(CCDParty ccdParty) {
        switch (ccdParty.getType()) {
            case COMPANY:
                return getCompany(ccdParty);
            case INDIVIDUAL:
                return getIndividual(ccdParty);
            case SOLE_TRADER:
                return getSoleTrader(ccdParty);
            case ORGANISATION:
                return getOrganisation(ccdParty);
            default:
                throw new MappingException();
        }
    }

    private TheirDetails getCompany(CCDParty ccdParty) {
        CCDCompany ccdCompany = ccdParty.getCompany();
        return new CompanyDetails(ccdCompany.getName(), addressMapper.from(ccdCompany.getAddress()),
            ccdCompany.getEmail(), representativeMapper.from(ccdCompany.getRepresentative()),
            addressMapper.from(ccdCompany.getCorrespondenceAddress()), ccdCompany.getContactPerson());
    }

    private TheirDetails getIndividual(CCDParty ccdParty) {
        CCDIndividual ccdIndividual = ccdParty.getIndividual();
        return new IndividualDetails(ccdIndividual.getName(),
            addressMapper.from(ccdIndividual.getAddress()),
            ccdIndividual.getEmail(),
            representativeMapper.from(ccdIndividual.getRepresentative()),
            addressMapper.from(ccdIndividual.getCorrespondenceAddress()),
            parseDob(ccdIndividual.getDateOfBirth())
        );
    }

    private LocalDate parseDob(String dateOfBirth) {
        if (StringUtils.isBlank(dateOfBirth)) {
            return null;
        }

        return LocalDate.parse(dateOfBirth, ISO_DATE);
    }

    private TheirDetails getSoleTrader(CCDParty ccdParty) {
        CCDSoleTrader ccdSoleTrader = ccdParty.getSoleTrader();
        return new SoleTraderDetails(ccdSoleTrader.getName(), addressMapper.from(ccdSoleTrader.getAddress()),
            ccdSoleTrader.getEmail(), representativeMapper.from(ccdSoleTrader.getRepresentative()),
            addressMapper.from(ccdSoleTrader.getCorrespondenceAddress()), ccdSoleTrader.getTitle(),
            ccdSoleTrader.getBusinessName());
    }

    private TheirDetails getOrganisation(CCDParty ccdParty) {
        CCDOrganisation ccdOrganisation = ccdParty.getOrganisation();
        return new OrganisationDetails(ccdOrganisation.getName(),
            addressMapper.from(ccdOrganisation.getAddress()), ccdOrganisation.getEmail(),
            representativeMapper.from(ccdOrganisation.getRepresentative()),
            addressMapper.from(ccdOrganisation.getCorrespondenceAddress()), ccdOrganisation.getContactPerson(),
            ccdOrganisation.getCompaniesHouseNumber());
    }
}
