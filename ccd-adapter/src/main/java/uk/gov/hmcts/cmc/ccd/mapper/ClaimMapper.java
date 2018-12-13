package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ClaimMapper implements BuilderMapper<CCDCase, ClaimData, CCDCase.CCDCaseBuilder> {

    private final PersonalInjuryMapper personalInjuryMapper;
    private final HousingDisrepairMapper housingDisrepairMapper;
    private final StatementOfTruthMapper statementOfTruthMapper;
    private ClaimantMapper claimantMapper;
    private final DefendantMapper defendantMapper;
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
        ClaimantMapper claimantMapper,
        DefendantMapper defendantMapper,
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
        this.claimantMapper = claimantMapper;
        this.defendantMapper = defendantMapper;
        this.amountMapper = amountMapper;
        this.paymentMapper = paymentMapper;
        this.interestMapper = interestMapper;
        this.timelineMapper = timelineMapper;
        this.evidenceMapper = evidenceMapper;
    }

    @Override
    public void to(ClaimData claimData, CCDCase.CCDCaseBuilder builder) {
        Objects.requireNonNull(claimData, "claimData must not be null");
//        CCDClaim.CCDClaimBuilder builder = CCDClaim.builder();
        claimData.getFeeCode().ifPresent(builder::feeCode);
        claimData.getFeeAccountNumber().ifPresent(builder::feeAccountNumber);
        claimData.getExternalReferenceNumber().ifPresent(builder::externalReferenceNumber);
        claimData.getPreferredCourt().ifPresent(builder::preferredCourt);

        claimData.getStatementOfTruth()
            .ifPresent(statementOfTruth -> statementOfTruthMapper.to(statementOfTruth, builder));

        claimData.getPersonalInjury().ifPresent(personalInjury -> personalInjuryMapper.to(personalInjury, builder));

        claimData.getHousingDisrepair()
            .ifPresent(housingDisrepair -> housingDisrepairMapper.to(housingDisrepair, builder));

        builder.claimants(claimData.getClaimants().stream().map(claimant -> claimantMapper.to(claimant))
            .map(this::mapClaimantToValue)
            .collect(Collectors.toList()));

        builder.defendants(claimData.getDefendants().stream().map(defendantMapper::to)
            .map(this::mapDefendantToValue)
            .collect(Collectors.toList()));

        claimData.getTimeline().ifPresent(timeline -> timelineMapper.to(timeline, builder));

        claimData.getEvidence().ifPresent(evidence -> evidenceMapper.to(evidence, builder));

        paymentMapper.to(claimData.getPayment(), builder);
        interestMapper.to(claimData.getInterest(), builder);
        amountMapper.to(claimData.getAmount(), builder);

        builder
            .reason(claimData.getReason())
            .feeAmountInPennies(claimData.getFeeAmountInPennies());
    }

    private CCDCollectionElement<CCDClaimant> mapClaimantToValue(CCDClaimant ccdParty) {
        return CCDCollectionElement.<CCDClaimant>builder().value(ccdParty).build();
    }

    private CCDCollectionElement<CCDDefendant> mapDefendantToValue(CCDDefendant ccdParty) {
        return CCDCollectionElement.<CCDDefendant>builder().value(ccdParty).build();
    }

    @Override
    public ClaimData from(CCDCase ccdCase) {
        Objects.requireNonNull(ccdCase, "ccdClaim must not be null");

        List<Party> claimants = ccdCase.getClaimants()
            .stream()
            .map(CCDCollectionElement::getValue)
            .map(claimantMapper::from)
            .collect(Collectors.toList());

        List<TheirDetails> defendants = ccdCase.getDefendants()
            .stream()
            .map(CCDCollectionElement::getValue)
            .map(defendantMapper::from)
            .collect(Collectors.toList());

        return new ClaimData(
            UUID.fromString(ccdCase.getExternalId()),
            claimants,
            defendants,
            paymentMapper.from(ccdCase),
            amountMapper.from(ccdCase),
            ccdCase.getFeeAmountInPennies(),
            interestMapper.from(ccdCase),
            personalInjuryMapper.from(ccdCase),
            housingDisrepairMapper.from(ccdCase),
            ccdCase.getReason(),
            statementOfTruthMapper.from(ccdCase),
            ccdCase.getFeeAccountNumber(),
            ccdCase.getExternalReferenceNumber(),
            ccdCase.getPreferredCourt(),
            ccdCase.getFeeCode(),
            timelineMapper.from(ccdCase),
            evidenceMapper.from(ccdCase)
        );
    }
}
