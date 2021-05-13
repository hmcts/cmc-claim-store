package uk.gov.hmcts.cmc.claimstore.repositories.elastic;

import org.assertj.core.api.Assertions;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QueryTest {

    @Test
    public void queryBuilderShouldNotBeNull() {
        Assertions.assertThatThrownBy(() -> new Query(null, 10, 0))
            .hasMessage("QueryBuilder cannot be null in search");
    }

    @Test
    public void pageSizeShouldNotBeLessThanOne() {
        Assertions.assertThatThrownBy(() -> new Query(QueryBuilders.matchAllQuery(), 0, 0))
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

        Query matchAll = new Query(QueryBuilders.matchAllQuery(), 10, 0);
        assertEquals(matchAllQueryString, matchAll.toString());
    }

}
