package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;

@Component
public class TheirDetailsMapper {

    private final IndividualDetailsMapper individualDetailsMapper;
    private final CompanyDetailsMapper companyDetailsMapper;
    private final OrganisationDetailsMapper organisationDetailsMapper;
    private final SoleTraderDetailsMapper soleTraderDetailsMapper;

    @Autowired
    public TheirDetailsMapper(
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

    public void to(CCDDefendant.CCDDefendantBuilder builder, TheirDetails theirDetails) {

        if (theirDetails instanceof IndividualDetails) {
            builder.claimantProvidedType(CCDPartyType.INDIVIDUAL);
            IndividualDetails individual = (IndividualDetails) theirDetails;
            individualDetailsMapper.to(individual, builder);
        } else if (theirDetails instanceof CompanyDetails) {
            builder.claimantProvidedType(CCDPartyType.COMPANY);
            CompanyDetails company = (CompanyDetails) theirDetails;
            companyDetailsMapper.to(company, builder);
        } else if (theirDetails instanceof OrganisationDetails) {
            builder.claimantProvidedType(CCDPartyType.ORGANISATION);
            OrganisationDetails organisation = (OrganisationDetails) theirDetails;
            organisationDetailsMapper.to(organisation, builder);
        } else if (theirDetails instanceof SoleTraderDetails) {
            builder.claimantProvidedType(CCDPartyType.SOLE_TRADER);
            SoleTraderDetails soleTrader = (SoleTraderDetails) theirDetails;
            soleTraderDetailsMapper.to(soleTrader, builder);
        }
    }

    public TheirDetails from(CCDCollectionElement<CCDDefendant> ccdDefendant) {
        switch (ccdDefendant.getValue().getClaimantProvidedType()) {
            case COMPANY:
                return companyDetailsMapper.from(ccdDefendant);
            case INDIVIDUAL:
                return individualDetailsMapper.from(ccdDefendant);
            case SOLE_TRADER:
                return soleTraderDetailsMapper.from(ccdDefendant);
            case ORGANISATION:
                return organisationDetailsMapper.from(ccdDefendant);
            default:
                throw new MappingException("Invalid defendant type, "
                    + ccdDefendant.getValue().getClaimantProvidedType());
        }
    }
}
