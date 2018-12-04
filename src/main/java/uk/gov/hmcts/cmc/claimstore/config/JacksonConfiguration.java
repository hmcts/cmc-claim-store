package uk.gov.hmcts.cmc.claimstore.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.cmc.ccd.deserialize.AmountBreakDownMixIn;
import uk.gov.hmcts.cmc.ccd.deserialize.CompanyDetailsMixIn;
import uk.gov.hmcts.cmc.ccd.deserialize.CompanyMixIn;
import uk.gov.hmcts.cmc.ccd.deserialize.IndividualDetailsMixIn;
import uk.gov.hmcts.cmc.ccd.deserialize.IndividualMixIn;
import uk.gov.hmcts.cmc.ccd.deserialize.OrganisationDetailsMixIn;
import uk.gov.hmcts.cmc.ccd.deserialize.OrganisationMixIn;
import uk.gov.hmcts.cmc.ccd.deserialize.RepresentativeMixIn;
import uk.gov.hmcts.cmc.ccd.deserialize.SoleTraderDetailsMixIn;
import uk.gov.hmcts.cmc.ccd.deserialize.SoleTraderMixIn;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

@Configuration
public class JacksonConfiguration {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Bean
    public ObjectMapper ccdObjectMapper() {
        return new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .addMixIn(Individual.class, IndividualMixIn.class)
            .addMixIn(SoleTrader.class, SoleTraderMixIn.class)
            .addMixIn(Company.class, CompanyMixIn.class)
            .addMixIn(Organisation.class, OrganisationMixIn.class)
            .addMixIn(IndividualDetails.class, IndividualDetailsMixIn.class)
            .addMixIn(SoleTraderDetails.class, SoleTraderDetailsMixIn.class)
            .addMixIn(CompanyDetails.class, CompanyDetailsMixIn.class)
            .addMixIn(OrganisationDetails.class, OrganisationDetailsMixIn.class)
            .addMixIn(AmountBreakDown.class, AmountBreakDownMixIn.class)
            .addMixIn(Representative.class, RepresentativeMixIn.class);

    }
}
