package uk.gov.hmcts.cmc.claimstore.documents;

import uk.gov.hmcts.cmc.domain.models.bulkprint.PrintRequestType;

public enum BulkPrintRequestType {
    FIRST_CONTACT_LETTER_TYPE("first-contact-pack",
        "Defendant first contact pack letter {} created for claim reference {}",
        PrintRequestType.PIN_LETTER_TO_DEFENDANT),
    DIRECTION_ORDER_LETTER_TYPE("direction-order-pack",
        "Direction order pack letter {} created for letter type {} claim reference {}",
        PrintRequestType.LEGAL_ORDER),
    GENERAL_LETTER_TYPE("general-letter",
        "General Letter {} created for letter type {} claim reference {}",
        PrintRequestType.GENERAL_LETTER),
    BULK_PRINT_TRANSFER_TYPE("bulk-print-transfer-pack",
        "Bulk print transfer to court {} created for letter type {} claim reference {}",
        PrintRequestType.BULK_PRINT_TRANSFER);

    String value;
    String logInfo;
    PrintRequestType printRequestType;

    BulkPrintRequestType(String value, String logInfo, PrintRequestType printRequestType) {
        this.value = value;
        this.logInfo = logInfo;
        this.printRequestType = printRequestType;
    }
}
