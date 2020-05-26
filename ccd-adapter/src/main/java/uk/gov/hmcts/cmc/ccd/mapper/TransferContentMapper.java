package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferContent;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferReason;
import uk.gov.hmcts.cmc.domain.models.TransferContent;

@Component
public class TransferContentMapper {
    private final AddressMapper addressMapper;

    public TransferContentMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public TransferContent from(CCDCase ccdCase) {

        CCDTransferContent ccdTransferContent = ccdCase.getTransferContent();

        if (ccdTransferContent == null) {
            return null;
        }

        return TransferContent.builder()
            .dateOfTransfer(ccdTransferContent.getDateOfTransfer())
            .reasonForTransfer(getReasonForTransfer(ccdTransferContent))
            .hearingCourtName(ccdCase.getHearingCourtName())
            .hearingCourtAddress(addressMapper.from(ccdCase.getHearingCourtAddress()))
            .build();
    }

    private static String getReasonForTransfer(CCDTransferContent ccdTransferContent) {
        return ccdTransferContent.getTransferReason() == CCDTransferReason.OTHER
            ? ccdTransferContent.getTransferReason().getValue()
            : ccdTransferContent.getTransferReasonOther();
    }
}
