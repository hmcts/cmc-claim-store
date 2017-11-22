package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.ccd.domain.CCDOrganisation;
import uk.gov.hmcts.cmc.ccd.domain.CCDRepresentative;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class OrganisationMapperTest {

    private AddressMapper addressMapper = new AddressMapper();
    private ContactDetailsMapper contactDetailsMapper = new ContactDetailsMapper();
    private RepresentativeMapper representativeMapper = new RepresentativeMapper(addressMapper, contactDetailsMapper);
    private OrganisationMapper organisationMapper = new OrganisationMapper(addressMapper, representativeMapper);


    @Test
    public void shouldMapOrganisationToCCD() {
        //given
        Organisation organisation = SampleParty.builder().organisation();

        //when
        CCDOrganisation ccdOrganisation = organisationMapper.to(organisation);

        //then
        assertThat(organisation).isEqualTo(ccdOrganisation);
    }

    @Test
    public void sholdMapOrganisationFromCCD() {
        //given
        final CCDAddress ccdAddress = CCDAddress.builder()
            .line1("line1")
            .line2("line1")
            .city("city")
            .postcode("postcode")
            .build();
        final CCDContactDetails ccdContactDetails = CCDContactDetails.builder()
            .phone("07987654321")
            .email(",my@email.com")
            .dxAddress("dx123")
            .build();
        CCDRepresentative ccdRepresentative = CCDRepresentative
            .builder()
            .organisationName("My Org")
            .organisationContactDetails(ccdContactDetails)
            .organisationAddress(ccdAddress)
            .build();
        CCDOrganisation ccdOrganisation = CCDOrganisation.builder()
            .name("Individual")
            .address(ccdAddress)
            .mobilePhone("07987654321")
            .correspondenceAddress(ccdAddress)
            .representative(ccdRepresentative)
            .contactPerson("MR. Hyde")
            .companiesHouseNumber("12345678")
            .build();

        //when
        Organisation organisation = organisationMapper.from(ccdOrganisation);

        //then
        assertThat(organisation).isEqualTo(ccdOrganisation);
    }

}
