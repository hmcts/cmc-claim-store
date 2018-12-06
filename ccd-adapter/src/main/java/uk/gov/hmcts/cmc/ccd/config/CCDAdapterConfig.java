package uk.gov.hmcts.cmc.ccd.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.cmc.ccd.jackson.custom.deserializer.ListItemDeserializer;
import uk.gov.hmcts.cmc.ccd.jackson.custom.serializer.ListItemSerializer;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.AmountBreakDownMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.AmountMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.AmountRangeMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.ClaimDataMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.ClaimMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.CompanyDetailsMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.CompanyMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.ContactDetailsMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.EvidenceMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.HousingDisrepairMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.IndividualDetailsMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.IndividualMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.InterestBreakDownMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.InterestDateMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.InterestMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.OrganisationDetailsMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.OrganisationMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.PaymentMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.PersonalInjuryMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.RepresentativeMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.SoleTraderDetailsMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.SoleTraderMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.StatementOfTruthMixIn;
import uk.gov.hmcts.cmc.ccd.jackson.mixin.TimelineMixIn;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.Timeline;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;
import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.particulars.HousingDisrepair;
import uk.gov.hmcts.cmc.domain.models.particulars.PersonalInjury;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

import java.util.List;


@Configuration
@ComponentScan(basePackages = {"uk.gov.hmcts.cmc"})
public class CCDAdapterConfig {
    @Bean
    public ObjectMapper ccdObjectMapper() {
        ListItemDeserializer listItemDeserializer = new ListItemDeserializer();
        ListItemSerializer listItemSerializer = new ListItemSerializer(List.class);
        return new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .registerModule(new SimpleModule().addDeserializer(List.class, listItemDeserializer))
            .registerModule(new SimpleModule().addSerializer(List.class, listItemSerializer))
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS)
            .addMixIn(Individual.class, IndividualMixIn.class)
            .addMixIn(SoleTrader.class, SoleTraderMixIn.class)
            .addMixIn(Company.class, CompanyMixIn.class)
            .addMixIn(Organisation.class, OrganisationMixIn.class)
            .addMixIn(IndividualDetails.class, IndividualDetailsMixIn.class)
            .addMixIn(SoleTraderDetails.class, SoleTraderDetailsMixIn.class)
            .addMixIn(CompanyDetails.class, CompanyDetailsMixIn.class)
            .addMixIn(OrganisationDetails.class, OrganisationDetailsMixIn.class)
            .addMixIn(Interest.class, InterestMixIn.class)
            .addMixIn(InterestBreakdown.class, InterestBreakDownMixIn.class)
            .addMixIn(InterestDate.class, InterestDateMixIn.class)
            .addMixIn(Evidence.class, EvidenceMixIn.class)
            .addMixIn(Timeline.class, TimelineMixIn.class)
            .addMixIn(Amount.class, AmountMixIn.class)
            .addMixIn(Payment.class, PaymentMixIn.class)
            .addMixIn(StatementOfTruth.class, StatementOfTruthMixIn.class)
            .addMixIn(AmountRange.class, AmountRangeMixIn.class)
            .addMixIn(AmountBreakDown.class, AmountBreakDownMixIn.class)
            .addMixIn(HousingDisrepair.class, HousingDisrepairMixIn.class)
            .addMixIn(PersonalInjury.class, PersonalInjuryMixIn.class)
            .addMixIn(ClaimData.class, ClaimDataMixIn.class)
            .addMixIn(ContactDetails.class, ContactDetailsMixIn.class)
            .addMixIn(Claim.class, ClaimMixIn.class)
            .addMixIn(Representative.class, RepresentativeMixIn.class);

    }
}
