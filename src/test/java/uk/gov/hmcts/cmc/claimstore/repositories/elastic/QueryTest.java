package uk.gov.hmcts.cmc.claimstore.repositories.elastic;

import org.assertj.core.api.Assertions;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QueryTest {

    @Test
    public void queryBuilderShouldNotBeNull() {
        Assertions.assertThatThrownBy(() -> new Query(null))
            .hasMessage("QueryBuilder cannot be null in search");

    }

    @Test
    public void queryToStringToMatch() {
        final String matchAllQueryString = "{\"query\": {\n"
            + "  \"match_all\" : {\n"
            + "    \"boost\" : 1.0\n"
            + "  }\n"
            + "}}";

        Query matchAll = new Query(QueryBuilders.matchAllQuery());
        assertEquals(matchAllQueryString, matchAll.toString());
    }

}
