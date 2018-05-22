package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaim;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ClaimMapper implements Mapper<CCDClaim, ClaimData> {

    private final PersonalInjuryMapper personalInjuryMapper;
    private final HousingDisrepairMapper housingDisrepairMapper;
    private final StatementOfTruthMapper statementOfTruthMapper;
    private final PartyMapper partyMapper;
    private final TheirDetailsMapper theirDetailsMapper;
    private final AmountMapper amountMapper;
    private final PaymentMapper paymentMapper;
    private final InterestMapper interestMapper;
    private final TimelineMapper timelineMapper;
    private final EvidenceMapper evidenceMapper;

    @SuppressWarnings("squid:S00107") //Constructor need all mapper for claim data  mapping
    public ClaimMapper(
        PersonalInjuryMapper personalInjuryMapper,
        HousingDisrepairMapper housingDisrepairMapper,
        StatementOfTruthMapper statementOfTruthMapper,
        PartyMapper partyMapper,
        TheirDetailsMapper theirDetailsMapper,
        AmountMapper amountMapper,
        PaymentMapper paymentMapper,
        InterestMapper interestMapper,
        InterestDateMapper interestDateMapper,
        TimelineMapper timelineMapper,
        EvidenceMapper evidenceMapper
    ) {
        this.personalInjuryMapper = personalInjuryMapper;
        this.housingDisrepairMapper = housingDisrepairMapper;
        this.statementOfTruthMapper = statementOfTruthMapper;
        this.partyMapper = partyMapper;
        this.theirDetailsMapper = theirDetailsMapper;
        this.amountMapper = amountMapper;
        this.paymentMapper = paymentMapper;
        this.interestMapper = interestMapper;
        this.timelineMapper = timelineMapper;
        this.evidenceMapper = evidenceMapper;
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

        claimData.getTimeline()
            .ifPresent(timeline -> builder.timeline(timelineMapper.to(timeline)));

        claimData.getEvidence()
            .ifPresent(evidence -> builder.evidence((evidenceMapper.to(evidence))));

        return builder
            .payment(paymentMapper.to(claimData.getPayment()))
            .interest(interestMapper.to(claimData.getInterest()))
            .reason(claimData.getReason())
            .amount(amountMapper.to(claimData.getAmount()))
            .feeAmountInPennies(claimData.getFeeAmountInPennies())
            .externalId(claimData.getExternalId().toString())
            .build();
    }

    private CCDCollectionElement<CCDParty> mapToValue(CCDParty ccdParty) {
        return CCDCollectionElement.<CCDParty>builder().value(ccdParty).build();
    }

    @Override
    public ClaimData from(CCDClaim ccdClaim) {
        Objects.requireNonNull(ccdClaim, "ccdClaim must not be null");

        List<Party> claimants = ccdClaim.getClaimants()
            .stream()
            .map(CCDCollectionElement::getValue)
            .map(partyMapper::from)
            .collect(Collectors.toList());

        List<TheirDetails> defendants = ccdClaim.getDefendants()
            .stream()
            .map(CCDCollectionElement::getValue)
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
            personalInjuryMapper.from(ccdClaim.getPersonalInjury()),
            housingDisrepairMapper.from(ccdClaim.getHousingDisrepair()),
            ccdClaim.getReason(),
            statementOfTruthMapper.from(ccdClaim.getStatementOfTruth()),
            ccdClaim.getFeeAccountNumber(),
            ccdClaim.getExternalReferenceNumber(),
            ccdClaim.getPreferredCourt(),
            ccdClaim.getFeeCode(),
            timelineMapper.from(ccdClaim.getTimeline()),
            evidenceMapper.from(ccdClaim.getEvidence()));
    }
}
