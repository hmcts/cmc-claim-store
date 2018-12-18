package uk.gov.hmcts.cmc.ccd.mapper;

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
import java.util.Optional;

@Component
public class DefendantMapper {

    private final IndividualDetailsMapper individualDetailsMapper;
    private final CompanyDetailsMapper companyDetailsMapper;
    private final OrganisationDetailsMapper organisationDetailsMapper;
    private final SoleTraderDetailsMapper soleTraderDetailsMapper;

    @Autowired
    public DefendantMapper(
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

    public CCDDefendant to(TheirDetails party, String letterHolderId, LocalDate responseDeadline) {
        CCDDefendant.CCDDefendantBuilder builder = CCDDefendant.builder();
        Optional.ofNullable(letterHolderId).ifPresent(builder::letterHolderId);
        Optional.ofNullable(responseDeadline).ifPresent(builder::responseDeadline);

        if (party instanceof IndividualDetails) {
            builder.claimantProvidedType(CCDPartyType.INDIVIDUAL);
            IndividualDetails individual = (IndividualDetails) party;
            individualDetailsMapper.to(individual, builder);
        } else if (party instanceof CompanyDetails) {
            builder.claimantProvidedType(CCDPartyType.COMPANY);
            CompanyDetails company = (CompanyDetails) party;
            companyDetailsMapper.to(company, builder);
        } else if (party instanceof OrganisationDetails) {
            builder.claimantProvidedType(CCDPartyType.ORGANISATION);
            OrganisationDetails organisation = (OrganisationDetails) party;
            organisationDetailsMapper.to(organisation, builder);
        } else if (party instanceof SoleTraderDetails) {
            builder.claimantProvidedType(CCDPartyType.SOLE_TRADER);
            SoleTraderDetails soleTrader = (SoleTraderDetails) party;
            soleTraderDetailsMapper.to(soleTrader, builder);
        }
        return builder.build();
    }

    public TheirDetails from(CCDDefendant ccdDefendant) {
        switch (ccdDefendant.getClaimantProvidedType()) {
            case COMPANY:
                return companyDetailsMapper.from(ccdDefendant);
            case INDIVIDUAL:
                return individualDetailsMapper.from(ccdDefendant);
            case SOLE_TRADER:
                return soleTraderDetailsMapper.from(ccdDefendant);
            case ORGANISATION:
                return organisationDetailsMapper.from(ccdDefendant);
            default:
                throw new MappingException();
        }
    }
}
