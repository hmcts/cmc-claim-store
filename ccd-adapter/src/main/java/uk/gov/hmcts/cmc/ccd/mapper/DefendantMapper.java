package uk.gov.hmcts.cmc.ccd.mapper;

import org.apache.commons.lang3.StringUtils;
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

import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ISO_DATE;

@Component
public class DefendantMapper implements Mapper<CCDDefendant, TheirDetails> {

    private final IndividualMapper individualMapper;
    private final IndividualDetailsMapper individualDetailsMapper;
    private final CompanyMapper companyMapper;
    private final CompanyDetailsMapper companyDetailsMapper;
    private final OrganisationMapper organisationMapper;
    private final OrganisationDetailsMapper organisationDetailsMapper;
    private final SoleTraderMapper soleTraderMapper;
    private final SoleTraderDetailsMapper soleTraderDetailsMapper;
    private final AddressMapper addressMapper;
    private final DefendantRepresentativeMapper representativeMapper;

    @Autowired
    public DefendantMapper(
        IndividualMapper individualMapper,
        IndividualDetailsMapper individualDetailsMapper,
        CompanyMapper companyMapper,
        CompanyDetailsMapper companyDetailsMapper,
        OrganisationMapper organisationMapper,
        OrganisationDetailsMapper organisationDetailsMapper,
        SoleTraderMapper soleTraderMapper,
        SoleTraderDetailsMapper soleTraderDetailsMapper,
        AddressMapper addressMapper,
        DefendantRepresentativeMapper representativeMapper
    ) {

        this.individualMapper = individualMapper;
        this.individualDetailsMapper = individualDetailsMapper;
        this.companyMapper = companyMapper;
        this.companyDetailsMapper = companyDetailsMapper;
        this.organisationMapper = organisationMapper;
        this.organisationDetailsMapper = organisationDetailsMapper;
        this.soleTraderMapper = soleTraderMapper;
        this.soleTraderDetailsMapper = soleTraderDetailsMapper;
        this.addressMapper = addressMapper;
        this.representativeMapper = representativeMapper;
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
                return new CompanyDetails(ccdDefendant.getPartyName(), addressMapper.from(ccdDefendant.getPartyAddress()),
                    ccdDefendant.getPartyEmail(),
                    representativeMapper.from(ccdDefendant),
                    addressMapper.from(ccdDefendant.getPartyServiceAddress()),
                    ccdDefendant.getPartyContactPerson());
            case INDIVIDUAL:
                return new IndividualDetails(ccdDefendant.getPartyName(), addressMapper.from(ccdDefendant.getPartyAddress()),
                    ccdDefendant.getPartyEmail(),
                    representativeMapper.from(ccdDefendant),
                    addressMapper.from(ccdDefendant.getPartyServiceAddress()),
                    parseDob(ccdDefendant.getPartyDateOfBirth()));
            case SOLE_TRADER:
                return new SoleTraderDetails(ccdDefendant.getPartyName(), addressMapper.from(ccdDefendant.getPartyAddress()),
                    ccdDefendant.getPartyEmail(),
                    representativeMapper.from(ccdDefendant),
                    addressMapper.from(ccdDefendant.getPartyServiceAddress()),
                    ccdDefendant.getPartyTitle(), ccdDefendant.getPartyBusinessName());
            case ORGANISATION:
                return new OrganisationDetails(ccdDefendant.getPartyName(), addressMapper.from(ccdDefendant.getPartyAddress()),
                    ccdDefendant.getPartyEmail(),
                    representativeMapper.from(ccdDefendant),
                    addressMapper.from(ccdDefendant.getPartyServiceAddress()),
                    ccdDefendant.getPartyContactPerson(), ccdDefendant.getPartyCompaniesHouseNumber());
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
