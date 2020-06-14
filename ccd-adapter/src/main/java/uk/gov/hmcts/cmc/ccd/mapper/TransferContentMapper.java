package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferContent;
import uk.gov.hmcts.cmc.ccd.domain.CCDTransferReason;
import uk.gov.hmcts.cmc.domain.models.TransferContent;

@Component
public class TransferContentMapper {
    private final AddressMapper addressMapper;

    public TransferContentMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public TransferContent from(CCDTransferContent transferContent) {
        if (transferContent == null) {
            return null;
        }

        return TransferContent.builder()
            .dateOfTransfer(transferContent.getDateOfTransfer())
            .reasonForTransfer(getReasonForTransfer(transferContent))
            .hearingCourtName(transferContent.getTransferCourtName())
            .hearingCourtAddress(addressMapper.from(transferContent.getTransferCourtAddress()))
            .build();
    }

    private static String getReasonForTransfer(CCDTransferContent ccdTransferContent) {
        return ccdTransferContent.getTransferReason() == CCDTransferReason.OTHER
            ? ccdTransferContent.getTransferReasonOther()
            : ccdTransferContent.getTransferReason().getValue();
    }
}
