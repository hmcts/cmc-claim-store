package uk.gov.hmcts.cmc.rpa.mapper.helper;

public class RPAMapperHelper {

    private RPAMapperHelper() {
        //NO-OP
    }

    public static String prependWithTradingAs(String value) {
        return "Trading as " + value;
    }
}
