package uk.gov.hmcts.cmc.rpa.mapper;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import uk.gov.hmcts.cmc.rpa.config.ModuleConfiguration;

import java.time.LocalDate;
import java.time.Month;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

@SpringBootTest
@ContextConfiguration(classes = ModuleConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings({"LineLength"})
public class AdmissionResponseJsonMapperTest extends BaseResponseJsonMapper {

    private static final String INDIVIDUAL_PART_ADMISSION_PAYING_IMMEDIATELY = "/admissions/individual_part_admission_immediately.json";
    private static final String INDIVIDUAL_PART_ADMISSION_BY_SET_DATE = "/admissions/individual_part_admission_by_set_date.json";
    private static final String INDIVIDUAL_PART_ADMISSION_BY_INSTALMENTS = "/admissions/individual_part_admission_by_instalments.json";
    private static final String INDIVIDUAL_PART_ADMISSION_STATES_PAID = "/admissions/individual_part_admission_states_paid.json";
    private static final String SOLE_TRADER_PART_ADMISSION_STATES_PAID = "/admissions/sole_trader_part_admission_states_paid.json";

    private static final String INDIVIDUAL_FULL_ADMISSION_BY_INSTALMENTS = "/admissions/individual_full_admission_by_instalments.json";
    private static final String INDIVIDUAL_FULL_ADMISSION_IMMEDIATELY = "/admissions/individual_full_admission_immediately.json";
    private static final String INDIVIDUAL_FULL_ADMISSION_BY_SET_DATE = "/admissions/individual_full_admission_by_set_date.json";
    private static final String SOLE_TRADER_ADDRESS_MODIFIED_BY_INSTALMENTS = "/admissions/sole_trader_part_admission_address_modified_corres_address.json";

    @Autowired
    private DefenceResponseJsonMapper responseMapper;

    @Test
    public void shouldMapIndividualPartAdmissionPayingImmediatelyToRPA() throws JSONException {
        PaymentIntention paymentIntention = SamplePaymentIntention.immediately();
        Party party = SampleParty.builder().withCorrespondenceAddress(null).individual();
        PartAdmissionResponse partAdmissionResponse = SampleResponse
            .PartAdmission.builder()
            .buildWithPaymentIntentionAndParty(paymentIntention, party);
        Claim claim = withCommonDefEmailAndRespondedAt()
            .withResponse(partAdmissionResponse)
            .build();

        String expected = new ResourceReader().read(INDIVIDUAL_PART_ADMISSION_PAYING_IMMEDIATELY).trim();

        assertEquals(expected, responseMapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapIndividualPartAdmissionPayingBySetDateToRPA() throws JSONException {
        LocalDate specifiedDate = LocalDate.of(2018, Month.JANUARY, 1);
        PaymentIntention paymentIntention = SamplePaymentIntention.bySetDateWithDateSpecified(specifiedDate);
        Party party = SampleParty.builder().withCorrespondenceAddress(null).individual();
        PartAdmissionResponse partAdmissionResponse = SampleResponse
            .PartAdmission.builder()
            .buildWithPaymentIntentionAndParty(paymentIntention, party);
        Claim claim = withCommonDefEmailAndRespondedAt()
            .withResponse(partAdmissionResponse)
            .build();

        String expected = new ResourceReader().read(INDIVIDUAL_PART_ADMISSION_BY_SET_DATE).trim();

        assertEquals(expected, responseMapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapIndividualPartAdmissionPayingByInstalmentsToRPA() throws JSONException {
        PaymentIntention paymentIntention = SamplePaymentIntention.instalments();
        Party party = SampleParty.builder().withCorrespondenceAddress(null).individual();
        PartAdmissionResponse partAdmissionResponse = SampleResponse
            .PartAdmission.builder()
            .buildWithPaymentIntentionAndParty(paymentIntention, party);
        Claim claim = withCommonDefEmailAndRespondedAt()
            .withResponse(partAdmissionResponse)
            .build();

        String expected = new ResourceReader().read(INDIVIDUAL_PART_ADMISSION_BY_INSTALMENTS).trim();

        assertEquals(expected, responseMapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapIndividualPartAdmissionStatesPaidToRPA() throws JSONException {
        PartAdmissionResponse partAdmissionStatesPaidResponse = SampleResponse
            .PartAdmission
            .builder()
            .buildWithStatesPaid(SampleParty.builder().individual());

        Claim claim = withCommonDefEmailAndRespondedAt()
            .withResponse(partAdmissionStatesPaidResponse)
            .build();

        String expected = new ResourceReader().read(INDIVIDUAL_PART_ADMISSION_STATES_PAID).trim();

        assertEquals(expected, responseMapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapSoleTraderPartAdmissionStatesPaidToRPA() throws JSONException {
        PartAdmissionResponse partAdmissionStatesPaidResponse = SampleResponse
            .PartAdmission
            .builder()
            .buildWithStatesPaid(SampleParty.builder().soleTrader());

        Claim claim = withCommonDefEmailAndRespondedAt()
            .withResponse(partAdmissionStatesPaidResponse)
            .build();

        String expected = new ResourceReader().read(SOLE_TRADER_PART_ADMISSION_STATES_PAID).trim();

        assertEquals(expected, responseMapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapIndividualFullAdmissionPayingByInstalmentsToRPA() throws JSONException {
        PaymentIntention paymentIntention = SamplePaymentIntention.instalments();
        Party party = SampleParty.builder().withCorrespondenceAddress(null).individual();
        FullAdmissionResponse fullAdmissionResponse = SampleResponse
            .FullAdmission.builder()
            .buildWithPaymentIntentionAndParty(paymentIntention, party);
        Claim claim = withCommonDefEmailAndRespondedAt()
            .withResponse(fullAdmissionResponse)
            .build();

        String expected = new ResourceReader().read(INDIVIDUAL_FULL_ADMISSION_BY_INSTALMENTS).trim();

        assertEquals(expected, responseMapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapIndividualFullAdmissionPayingByImmediatelyToRPA() throws JSONException {
        PaymentIntention paymentIntention = SamplePaymentIntention.immediately();
        Party party = SampleParty.builder().withCorrespondenceAddress(null).individual();
        FullAdmissionResponse fullAdmissionResponse = SampleResponse
            .FullAdmission.builder()
            .buildWithPaymentIntentionAndParty(paymentIntention, party);
        Claim claim = withCommonDefEmailAndRespondedAt()
            .withResponse(fullAdmissionResponse)
            .build();

        String expected = new ResourceReader().read(INDIVIDUAL_FULL_ADMISSION_IMMEDIATELY).trim();

        assertEquals(expected, responseMapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapIndividualFullAdmissionPayingBySetDateToRPA() throws JSONException {
        LocalDate specifiedDate = LocalDate.of(2018, Month.JANUARY, 1);
        PaymentIntention paymentIntention = SamplePaymentIntention.bySetDateWithDateSpecified(specifiedDate);
        Party party = SampleParty.builder().withCorrespondenceAddress(null).individual();
        FullAdmissionResponse fullAdmissionResponse = SampleResponse
            .FullAdmission.builder()
            .buildWithPaymentIntentionAndParty(paymentIntention, party);
        Claim claim = withCommonDefEmailAndRespondedAt()
            .withResponse(fullAdmissionResponse)
            .build();

        String expected = new ResourceReader().read(INDIVIDUAL_FULL_ADMISSION_BY_SET_DATE).trim();

        assertEquals(expected, responseMapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapSoleTraderPartAdmissionWithAddressModifiedPayingByInstalmentsToRPA() throws JSONException {
        PaymentIntention instalments = SamplePaymentIntention.instalments();
        Address modifiedAddress = SampleAddress.builder().city("London").build();
        Party soleTrader = SampleParty.builder().withAddress(modifiedAddress).soleTrader();
        PartAdmissionResponse partAdmissionResponse = SampleResponse
            .PartAdmission.builder()
            .buildWithPaymentIntentionAndParty(instalments, soleTrader);
        Claim claim = withCommonDefEmailAndRespondedAt()
            .withResponse(partAdmissionResponse)
            .build();

        String expected = new ResourceReader().read(SOLE_TRADER_ADDRESS_MODIFIED_BY_INSTALMENTS).trim();

        assertEquals(expected, responseMapper.map(claim).toString(), STRICT);
    }
}
