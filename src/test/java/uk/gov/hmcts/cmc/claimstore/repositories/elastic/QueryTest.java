package uk.gov.hmcts.cmc.claimstore.repositories.elastic;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryTest {

    @Test
    public void queryBuilderShouldNotBeNull() {
        Assertions.assertThatThrownBy(() -> new Query(null, 10, 0))
            .hasMessage("QueryBuilder cannot be null in search");
    }

    @Test
    public void pageSizeShouldNotBeLessThanOne() {
        Assertions.assertThatThrownBy(() -> new Query(Queries.matchAllQuery(), 0, 0))
            .hasMessage("Page size cant be less than 1");
    }

    @Test
    public void queryToStringToMatch() {
        final String matchAllQueryString = "{"
            + "\"size\": 10,"
            + "\"from\": " + 0 + ","
            + "\"sort\": " + "[\n"
            + "    { \"created_date\" : {\"order\" : \"desc\"}}\n"
            + "    ]" + ","
            + "\"query\": {\n"
            + "  \"match_all\" : {\n"
            + "    \"boost\" : 1.0\n"
            + "  }\n"
            + "}}";

        Query matchAll = new Query(Queries.matchAllQuery(), 10, 0);
        assertEquals(matchAllQueryString, matchAll.toString());
    }

    @Test
    public void queryToStringIncludesPageOffset() {
        final String pagedQuery = "{"
            + "\"size\": 10,"
            + "\"from\": " + 20 + ","
            + "\"sort\": " + "[\n"
            + "    { \"created_date\" : {\"order\" : \"desc\"}}\n"
            + "    ]" + ","
            + "\"query\": {\n"
            + "  \"match_all\" : {\n"
            + "    \"boost\" : 1.0\n"
            + "  }\n"
            + "}}";

        Query page = new Query(Queries.matchAllQuery(), 10, 20);
        assertEquals(pagedQuery, page.toString());
    }

}
