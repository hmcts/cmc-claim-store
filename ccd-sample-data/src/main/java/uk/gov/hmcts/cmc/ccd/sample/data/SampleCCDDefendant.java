package uk.gov.hmcts.cmc.ccd.sample.data;

import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDTelephone;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDPartyStatement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDMadeBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.COMPANY;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.INDIVIDUAL;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefenceType.ALREADY_PAID;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseType.PART_ADMISSION;
import static uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceType.OTHER;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getCCDAddress;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getCCDPaymentIntention;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getCCDStatementOfMeans;

public class SampleCCDDefendant {

    private SampleCCDDefendant() {
        //Utility class
    }

    public static CCDRespondent.CCDRespondentBuilder withDefault() {
        return CCDRespondent.builder()
            .claimantProvidedDetail(CCDParty.builder()
                .type(INDIVIDUAL)
                .emailAddress("defendant@Ididabadjob.com")
                .build())
            .defendantId("defendantId")
            .letterHolderId("JCJEDU")
            .servedDate(now().plusDays(5))
            .responseDeadline(now().plusDays(14));
    }

    public static CCDRespondent.CCDRespondentBuilder withResponseMoreTimeNeededOption() {
        return withDefault()
            .partyDetail(CCDParty.builder().emailAddress("defendant@Ididabadjob.com").build())
            .responseMoreTimeNeededOption(NO);
    }

    private static CCDParty.CCDPartyBuilder withPartyDetails() {
        return CCDParty.builder().type(COMPANY)
            .primaryAddress(getCCDAddress())
            .correspondenceAddress(getCCDAddress())
            .telephoneNumber(
                CCDTelephone.builder()
                    .telephoneNumber("07123456789")
                    .build()
            );
    }

    private static CCDRespondent.CCDRespondentBuilder withParty() {

        return CCDRespondent.builder()
            .partyName("Mr Norman")
            .representativeOrganisationName("Trading ltd")
            .representativeOrganisationAddress(getCCDAddress())
            .representativeOrganisationPhone("07123456789")
            .representativeOrganisationEmail("representative@example.org")
            .representativeOrganisationDxAddress("DX123456");
    }

    public static CCDRespondent.CCDRespondentBuilder withPartyIndividual() {
        return withParty().partyDetail(
            withPartyDetails()
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .build());
    }

    public static CCDRespondent.CCDRespondentBuilder withPartyCompany() {
        return withParty()
            .partyDetail(withPartyDetails()
                .contactPerson("Mr Steven")
                .build());
    }

    public static CCDRespondent.CCDRespondentBuilder withPartySoleTrader() {
        return withParty()
            .partyDetail(withPartyDetails()
                .title("Mr")
                .businessName("Trading as name")
                .build());
    }

    public static CCDRespondent.CCDRespondentBuilder withPartyOrganisation() {
        return withParty()
            .partyDetail(withPartyDetails()
                .contactPerson("Mr Steven")
                .companiesHouseNumber("12345")
                .build());
    }

    private static CCDRespondent.CCDRespondentBuilder withResponse() {
        return withPartyIndividual()
            .responseMoreTimeNeededOption(NO)
            .responseFreeMediationOption(NO)
            .responseDefendantSOTSignerName("Signer name")
            .responseDefendantSOTSignerRole("Signer role");
    }

    public static CCDRespondent.CCDRespondentBuilder withFullDefenceResponse() {
        return withResponse()
            .responseType(FULL_DEFENCE)
            .responseDefenceType(ALREADY_PAID)
            .responseDefence("This is my defence")
            .paymentDeclarationPaidDate(now())
            .paymentDeclarationExplanation("Payment declaration explanation")
            .defendantTimeLineComment("Time line comments")
            .defendantTimeLineEvents(asList(
                CCDCollectionElement.<CCDTimelineEvent>builder().value(CCDTimelineEvent.builder()
                    .date("Time of event")
                    .description("Description of the event")
                    .build()
                ).build()
            ))
            .responseEvidenceComment("Evidence comments")
            .responseEvidenceRows(
                asList(
                    CCDCollectionElement.<CCDEvidenceRow>builder().value(CCDEvidenceRow.builder()
                        .type(OTHER)
                        .description("My description")
                        .build()
                    ).build()
                ));
    }

    public static CCDRespondent.CCDRespondentBuilder withFullDefenceResponseAndFreeMediation() {
        return withFullDefenceResponse()
            .responseFreeMediationOption(YES)
            .responseMediationPhoneNumber(CCDTelephone.builder().telephoneNumber("07999999999").build())
            .responseMediationContactPerson("Mediation Contact Person");
    }

    public static CCDRespondent.CCDRespondentBuilder withFullAdmissionResponse() {
        return withResponse()
            .responseType(FULL_ADMISSION)
            .statementOfMeans(getCCDStatementOfMeans())
            .defendantPaymentIntention(getCCDPaymentIntention());
    }

    public static CCDRespondent.CCDRespondentBuilder withReDetermination() {
        return withParty()
            .redeterminationMadeBy(CCDMadeBy.CLAIMANT)
            .redeterminationExplanation("Need money sooner")
            .redeterminationRequestedDate(LocalDateTime.now());
    }

    public static CCDRespondent.CCDRespondentBuilder withPartAdmissionResponse() {
        return withResponse()
            .responseType(PART_ADMISSION)
            .responseAmount("1000")
            .paymentDeclarationPaidDate(now())
            .paymentDeclarationExplanation("Payment declaration explanation")
            .defendantPaymentIntention(getCCDPaymentIntention())
            .responseDefence("This is my defence")
            .statementOfMeans(getCCDStatementOfMeans())
            .defendantTimeLineComment("Time line comments")
            .defendantTimeLineEvents(asList(
                CCDCollectionElement.<CCDTimelineEvent>builder().value(CCDTimelineEvent.builder()
                    .date("Time of event")
                    .description("Description of the event")
                    .build()
                ).build()
            ))
            .responseEvidenceComment("Evidence comments")
            .responseEvidenceRows(
                asList(
                    CCDCollectionElement.<CCDEvidenceRow>builder().value(CCDEvidenceRow.builder()
                        .type(OTHER)
                        .description("My description")
                        .build()
                    ).build()
                ));
    }

    public static CCDRespondent.CCDRespondentBuilder withPaidInFull(LocalDate paidInFullDate) {
        return withDefault().paidInFullDate(paidInFullDate);
    }

    public static CCDRespondent.CCDRespondentBuilder withPartyStatements() {
        List<CCDCollectionElement<CCDPartyStatement>> partyStatements =
            asList(
                CCDCollectionElement.<CCDPartyStatement>builder()
                    .value(SampleCCDPartyStatement.offerPartyStatement()).build(),
                CCDCollectionElement.<CCDPartyStatement>builder()
                    .value(SampleCCDPartyStatement.acceptPartyStatement()).build(),
                CCDCollectionElement.<CCDPartyStatement>builder()
                    .value(SampleCCDPartyStatement.counterSignPartyStatement()).build()
            );
        return withPartAdmissionResponse()
            .claimantProvidedDetail(CCDParty.builder().type(COMPANY).build())
            .settlementReachedAt(LocalDateTime.now())
            .settlementPartyStatements(partyStatements);
    }

    public static CCDRespondent.CCDRespondentBuilder withMediationAgreementDate(LocalDateTime localDatetime) {
        return withDefault().mediationSettlementReachedAt(localDatetime);
    }

    public static CCDRespondent.CCDRespondentBuilder withMediationFailureReason() {
        return withDefault().mediationFailedReason("Defendant phone died while mediation call");
    }
}
