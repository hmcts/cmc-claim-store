package uk.gov.hmcts.cmc.ccd.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaim;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ClaimMapper implements Mapper<CCDClaim, ClaimData> {
    private final Logger logger = LoggerFactory.getLogger(ClaimMapper.class);

    private static final String SERIALISATION_ERROR_MESSAGE = "Failed to serialize '%s' to JSON";
    private static final String DESERIALISATION_ERROR_MESSAGE = "Failed to deserialize '%s' from JSON";
    private static final String COLLECTION_KEY_NAME = "value";
    private final PersonalInjuryMapper personalInjuryMapper;
    private final HousingDisrepairMapper housingDisrepairMapper;
    private final StatementOfTruthMapper statementOfTruthMapper;
    private final PartyMapper partyMapper;
    private final TheirDetailsMapper theirDetailsMapper;
    private final AmountMapper amountMapper;
    private final PaymentMapper paymentMapper;
    private final InterestMapper interestMapper;
    private final InterestDateMapper interestDateMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    @SuppressWarnings("squid:S00107") //Constructor need all mapper for claim data  mapping
    public ClaimMapper(PersonalInjuryMapper personalInjuryMapper,
                       HousingDisrepairMapper housingDisrepairMapper,
                       StatementOfTruthMapper statementOfTruthMapper,
                       PartyMapper partyMapper,
                       TheirDetailsMapper theirDetailsMapper,
                       AmountMapper amountMapper,
                       PaymentMapper paymentMapper,
                       InterestMapper interestMapper,
                       InterestDateMapper interestDateMapper) {

        this.personalInjuryMapper = personalInjuryMapper;
        this.housingDisrepairMapper = housingDisrepairMapper;
        this.statementOfTruthMapper = statementOfTruthMapper;
        this.partyMapper = partyMapper;
        this.theirDetailsMapper = theirDetailsMapper;
        this.amountMapper = amountMapper;
        this.paymentMapper = paymentMapper;
        this.interestMapper = interestMapper;
        this.interestDateMapper = interestDateMapper;
    }

    @Override
    public CCDClaim to(ClaimData claimData) {
        Objects.requireNonNull(claimData, "claimData must not be null");
        CCDClaim.CCDClaimBuilder builder = CCDClaim.builder();
        claimData.getFeeCode().ifPresent(builder::feeCode);
        claimData.getFeeAccountNumber().ifPresent(builder::feeAccountNumber);
        claimData.getExternalReferenceNumber().ifPresent(builder::externalReferenceNumber);
        claimData.getPreferredCourt().ifPresent(builder::preferredCourt);

        claimData.getStatementOfTruth()
            .ifPresent(statementOfTruth -> builder.statementOfTruth(statementOfTruthMapper.to(statementOfTruth)));

        claimData.getPersonalInjury()
            .ifPresent(personalInjury -> builder.personalInjury(personalInjuryMapper.to(personalInjury)));

        claimData.getHousingDisrepair()
            .ifPresent(housingDisrepair -> builder.housingDisrepair(housingDisrepairMapper.to(housingDisrepair)));

        builder.claimants(claimData.getClaimants().stream().map(partyMapper::to)
            .map(this::mapToValue)
            .collect(Collectors.toList()));

        builder.defendants(claimData.getDefendants().stream().map(theirDetailsMapper::to)
            .map(this::mapToValue)
            .collect(Collectors.toList()));

        return builder
            .payment(paymentMapper.to(claimData.getPayment()))
            .interest(interestMapper.to(claimData.getInterest()))
            .interestDate(interestDateMapper.to(claimData.getInterestDate()))
            .reason(claimData.getReason())
            .amount(amountMapper.to(claimData.getAmount()))
            .feeAmountInPennies(claimData.getFeeAmountInPennies())
            .externalId(claimData.getExternalId().toString())
            .build();
    }

    private Map<String, Object> mapToValue(Object ccdParty) {
        return Collections.singletonMap(COLLECTION_KEY_NAME, ccdParty);
    }

    @Override
    public ClaimData from(CCDClaim ccdClaim) {
        Objects.requireNonNull(ccdClaim, "ccdClaim must not be null");

        List<Party> claimants = ccdClaim.getClaimants()
            .stream()
            .filter(c -> c.containsKey("value"))
            .map(this::valueFromMap)
            .map(this::toJson)
            .map(s -> this.fromJson(s, CCDParty.class))
            .map(partyMapper::from)
            .collect(Collectors.toList());

        List<TheirDetails> defendants = ccdClaim.getDefendants()
            .stream()
            .filter(c -> c.containsKey("value"))
            .map(this::valueFromMap)
            .map(this::toJson)
            .map(s -> this.fromJson(s, CCDParty.class))
            .map(theirDetailsMapper::from)
            .collect(Collectors.toList());

        return new ClaimData(
            UUID.fromString(ccdClaim.getExternalId()),
            claimants,
            defendants,
            paymentMapper.from(ccdClaim.getPayment()),
            amountMapper.from(ccdClaim.getAmount()),
            ccdClaim.getFeeAmountInPennies(),
            interestMapper.from(ccdClaim.getInterest()),
            interestDateMapper.from(ccdClaim.getInterestDate()),
            personalInjuryMapper.from(ccdClaim.getPersonalInjury()),
            housingDisrepairMapper.from(ccdClaim.getHousingDisrepair()),
            ccdClaim.getReason(),
            statementOfTruthMapper.from(ccdClaim.getStatementOfTruth()),
            ccdClaim.getFeeAccountNumber(),
            ccdClaim.getExternalReferenceNumber(),
            ccdClaim.getPreferredCourt(),
            ccdClaim.getFeeCode());
    }

    private Object valueFromMap(Map<String, Object> value) {
        return value.get(COLLECTION_KEY_NAME);
    }

    private String toJson(Object input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            logger.info(e.getMessage(), e);
            throw new RuntimeException(
                String.format(SERIALISATION_ERROR_MESSAGE, input.getClass().getSimpleName()), e
            );
        }
    }

    private <T> T fromJson(String value, Class<T> clazz) {
        try {
            return objectMapper.readValue(value, clazz);
        } catch (IOException e) {
            logger.info(e.getMessage(), e);
            throw new RuntimeException(
                String.format(DESERIALISATION_ERROR_MESSAGE, clazz.getSimpleName()), e
            );
        }
    }
}
